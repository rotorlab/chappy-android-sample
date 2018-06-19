package com.rotor.core

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.rotor.core.interfaces.REvent
import com.google.gson.Gson
import com.rotor.core.interfaces.BuilderFace
import org.json.JSONObject

/**
 * Created by efraespada on 11/03/2018.
 */

class Rotor {

    companion object {

        private val TAG = Rotor::class.java.simpleName
        internal val PREF_ID = "rotor_id"
        internal val PREF_URL = "rotor_url"
        internal val PREF_CONFIG = "rotor_config"

        var context: Context? = null
        @JvmStatic var id: String ? = null
        @JvmStatic var urlServer: String ? = null
        @JvmStatic var urlRedis: String ? = null
        lateinit var REvent: REvent
        private var jobId = 0
        private var serviceComponent: ComponentName ? = null

        var gson: Gson? = null
        var debug: Boolean? = null
        var initialize: Boolean = false
        var builders: HashMap<Builder, BuilderFace> ? = null

        @JvmStatic fun initialize(context: Context, urlServer: String, redisServer: String, REvent: REvent) {
            this@Companion.context = context
            this@Companion.urlServer = urlServer
            this@Companion.urlRedis = redisServer
            this@Companion.REvent = REvent
            if (builders == null) {
                builders = HashMap<Builder, BuilderFace>()
            }
            debug = false
            gson = Gson()
            val shared = context.getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
            id = shared.getString(PREF_ID, null)
            if (id == null) {
                id = generateNewId()
            }

            serviceComponent = ComponentName(context, JobRotorService::class.java)

            initialize = false

            stop()
            start()
        }

        private fun generateNewId(): String {
            val id = Settings.Secure.getString(context!!.getContentResolver(), Settings.Secure.ANDROID_ID)
            val shared = context!!.getSharedPreferences(PREF_CONFIG, MODE_PRIVATE).edit()
            shared.putString(PREF_ID, id)
            shared.apply()
            return id
        }

        fun stop() {
            try {
                finishJob()
                context?.stopService(Intent(context, JobRotorService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun start() {
            val startServiceIntent = Intent(context, JobRotorService::class.java)
            context?.startService(startServiceIntent)

            scheduleJob()
        }

        @JvmStatic fun onMessageReceived(jsonObject: JSONObject) {
            if (builders != null) {
                for (face in builders!!.values) {
                    face.onMessageReceived(jsonObject)
                }
            }
        }

        @JvmStatic fun prepare(type: Builder, face: BuilderFace) {
            if (builders != null) {
                builders!![type] = face
            }
        }

        @JvmStatic fun debug(debug: Boolean) {
            this@Companion.debug = debug
        }

        fun scheduleJob() {
            val builder = JobInfo.Builder(jobId++, serviceComponent!!)
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

            Log.d(TAG, "Scheduling job")
            (context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
        }

        private fun finishJob() {
            (context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
        }

        internal fun connected() {
            initialize = true
            REvent.connected()
        }

        internal fun notConnected() {
            initialize = false
            REvent.reconnecting()
        }

        fun isConnected() : Boolean {
            return initialize
        }

    }


}