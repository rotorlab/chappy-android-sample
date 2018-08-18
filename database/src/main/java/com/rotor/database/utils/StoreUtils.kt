package com.rotor.database.utils

import android.content.Context
import android.content.SharedPreferences
import com.rotor.core.Rotor

import com.rotor.database.interfaces.Server
import com.stringcare.library.SC
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger

/**
 * Created by efraespada on 14/03/2018.
 */
class StoreUtils {

    companion object {

        private var retrofits: HashMap<String, Server> ? = null

        private fun pref(context: Context) : SharedPreferences {
            return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE);
        }

        private fun editor(context: Context) : SharedPreferences.Editor {
            return pref(context).edit();
        }

        fun addElement(path: String, info: String) {
            doAsync {
                val enPath = SC.encryptString(path)
                val enInfo = SC.encryptString(info)
                editor(Rotor.context!!).putString(enPath, enInfo).commit()
            }
        }

        fun exist(path: String): Boolean {
            val enPath = SC.encryptString(path)
            return pref(Rotor.context!!).getString(enPath, null) == null
        }

        fun removeElement(path: String) {
            doAsync {
                val enPath = SC.encryptString(path)
                editor(Rotor.context!!).remove(enPath).commit()
            }
        }


        /**
         * returns stored object
         * @param context
         * @param path
         * @return String
         */
        fun getElement(path: String): String? {
            val enPath = SC.encryptString(path)
            return SC.decryptString(pref(Rotor.context!!).getString(enPath, null))
        }

        private fun string2Hex(data: ByteArray): String {
            return BigInteger(1, data).toString(16)
        }

        fun hex2String(value: String): String {
            return String(BigInteger(value, 16).toByteArray())
        }

        fun service(url: String): Server {
            if (retrofits == null) {
                retrofits = HashMap()
            }
            if (!retrofits!!.containsKey(url)) {
                val retrofitBuilder = Retrofit.Builder()
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(url)
                        .build()

                retrofits!![url] = retrofitBuilder.create(Server::class.java)
            }
            return retrofits!![url]!!
        }

    }

}