package com.rotor.database.models

import android.content.Context
import com.google.common.reflect.TypeToken
import com.rotor.database.Database
import com.rotor.database.abstr.Reference
import com.rotor.database.utils.StoreUtils
import java.lang.reflect.Type

/**
 * Created by efraespada on 14/03/2018.
 */
class KReference<T>(context: Context, database: String, path: String, reference: Reference<*>, moment: Long) : PrimaryReferece<Reference<T>>(context, database, path) {

    private val clazz: Class<T>

    init {
        blowerMap[moment] = reference as Reference<T>
        this.clazz = reference.clazz()
    }

    override fun getLastest(): Reference<T> {
        var lastest: Long = 0
        var blower: Reference<T>? = null
        // TODO limit list of blowers
        for (entry in blowerMap.entries) {
            if (lastest < entry.key) {
                lastest = entry.key
                blower = entry.value
            }
        }
        return blower!!
    }

    override fun progress(value: Int) {
        for (entry in blowerMap.entries) {
            entry.value.progress(value)
        }
    }

    override fun getReferenceAsString(): String {
        var value: String ?
        if (getLastest().onUpdate() == null) {
            if (stringReference != null && stringReference!!.length > EMPTY_OBJECT.length) {
                value = stringReference
            } else {
                value = EMPTY_OBJECT
            }
        } else {
            value = gson.toJson(getLastest().onUpdate(), clazz)
        }
        return value!!
    }

    override fun loadCachedReference() {
        stringReference = StoreUtils.getElement(path)
        if (stringReference != null && stringReference!!.length > EMPTY_OBJECT.length) {
            blowerResult(stringReference!!)
        }
    }

    override fun blowerResult(value: String) {
        val obj: T = gson.fromJson(value, getType())
        Database.backgroundHandler()!!.addPath(path, this)
        for (entry in blowerMap.entries) {
            entry.value.onChanged(obj)
        }
    }

    override fun remove() {
        StoreUtils.removeElement(path)
        for (entry in blowerMap.entries) {
            entry.value.onDestroy()
        }

    }

    fun getType(): Type {
        return TypeToken.of(clazz).type
    }

}