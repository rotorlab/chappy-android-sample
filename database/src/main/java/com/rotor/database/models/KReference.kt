package com.rotor.database.models

import android.content.Context
import com.google.common.reflect.TypeToken
import com.rotor.database.abstr.Reference
import com.rotor.database.utils.ReferenceUtils
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

    fun addBlower(creation: Long, blower: Reference<*>) {
        blowerMap[creation] = blower as Reference<T>
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
        stringReference = ReferenceUtils.getElement(path)
        if (stringReference != null && stringReference!!.length > EMPTY_OBJECT.length) {
            blowerResult(stringReference!!)
        }
    }

    override fun blowerResult(value: String) {
        for (entry in blowerMap.entries) {
            entry.value.onChanged(gson.fromJson(value, getType()))
        }
    }

    override fun remove() {
        ReferenceUtils.removeElement(path)
        for (entry in blowerMap.entries) {
            entry.value.onDestroy()
        }

    }

    fun getType(): Type {
        return TypeToken.of(clazz).type
    }

}