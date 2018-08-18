package com.rotor.database.utils

import com.google.gson.Gson
import com.rotor.core.Rotor
import com.rotor.core.interfaces.RScreen
import com.rotor.database.Database
import com.rotor.database.models.KReference
import com.rotor.database.models.PrimaryReferece
import org.jetbrains.anko.doAsync

class BackgroundHandler: RScreen {

    private val map = HashMap<String, Any>()

    override fun isActive(): Boolean {
        return true
    }

    override fun addPath(path: String, obj: Any): Boolean {
        map.put(path, obj)
        return true
    }

    override fun removePath(path: String): Boolean {
        if (map.containsKey(path)) {
            map.remove(path)
            return true
        }
        return false
    }

    override fun hasPath(path: String): Boolean {
        return map.containsKey(path)
    }

    override fun holders(): java.util.HashMap<String, Any> {
        return map
    }

    override fun connected() {
        // nothing to do here
    }

    override fun disconnected() {
        // nothing to do here
    }

    fun <T> getReference(path: String, klass: Class<T>) : T ? {
        if (map.containsKey(path)) {
            val gson = Gson()
            return gson.fromJson((map[path] as KReference<T>).getReferenceAsString(), klass)
        }
        return null
    }

    fun <T> sync(path: String, reference: T) {
        doAsync {
            val refe = map[path] as KReference<*>
            val result = refe.getDifferencesFromBackground(reference!!)
            val diff = result[1] as String
            val len = result[0] as Int
            if (!PrimaryReferece.EMPTY_OBJECT.equals(diff)) {
                Database.refreshToServer(path, diff, len, false)
            } else {
                val blower = refe.getLastest()
                val value = refe.getReferenceAsString()
                if (value.equals(PrimaryReferece.EMPTY_OBJECT) || value.equals(PrimaryReferece.NULL)) {
                    blower.onCreate()
                }
            }
        }
    }

}