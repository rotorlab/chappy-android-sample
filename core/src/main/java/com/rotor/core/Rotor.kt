package com.rotor.core

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.rotor.core.interfaces.InternalServiceListener
import com.rotor.core.interfaces.StatusListener
import com.google.gson.Gson
import com.rotor.core.RotorService.Companion.PREF_CONFIG
import com.rotor.core.RotorService.Companion.PREF_ID
import com.rotor.core.interfaces.BuilderFace
import org.json.JSONObject

/**
 * Created by efraespada on 11/03/2018.
 */

class Rotor {

    companion object {

        private val TAG = Rotor::class.java.simpleName

        var context: Context? = null
        @JvmStatic var id: String ? = null
        @JvmStatic var urlServer: String ? = null
        @JvmStatic var urlRedis: String ? = null
        lateinit var statusListener: StatusListener
        private var jobId = 0
        private var serviceComponent: ComponentName ? = null

        var gson: Gson? = null
        var debug: Boolean? = null
        var initialize: Boolean = false
        var builders: HashMap<Builder, BuilderFace> ? = null

        @JvmStatic fun initialize(context: Context, urlServer: String, redisServer: String, statusListener: StatusListener) {
            Companion.context = context
            Companion.urlServer = urlServer
            Companion.urlRedis = redisServer
            Companion.statusListener = statusListener
            if (Companion.builders == null) {
                Companion.builders = HashMap<Builder, BuilderFace>()
            }
            Companion.debug = false
            Companion.gson = Gson()
            val shared = context.getSharedPreferences(PREF_CONFIG, MODE_PRIVATE)
            Companion.id = shared.getString(PREF_ID, null)
            if (Companion.id == null) {
                Companion.id = generateNewId()
            }

            serviceComponent = ComponentName(context, JobRotorService::class.java)

            Companion.initialize = false

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

        @JvmStatic fun onResume() {
            // start()
        }

        @JvmStatic fun onPause() {
            //stop()
        }

        @JvmStatic fun onMessageReceived(jsonObject: JSONObject) {
            if (Companion.builders != null) {
                for (face in Companion.builders!!.values) {
                    face.onMessageReceived(jsonObject)
                }
            }
        }

        @JvmStatic fun prepare(type: Builder, face: BuilderFace) {
            if (Companion.builders != null) {
                Companion.builders!![type] = face
            }
        }

        @JvmStatic fun debug(debug: Boolean) {
            Companion.debug = debug
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
            statusListener.connected()
        }

        internal fun notConnected() {
            initialize = false
            statusListener.reconnecting()
        }

        fun isConnected() : Boolean {
            return initialize
        }

    }


}