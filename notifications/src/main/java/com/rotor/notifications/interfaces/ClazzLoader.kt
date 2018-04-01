package com.rotor.notifications.interfaces

/**
 * Created by efraespada on 23/03/2018.
 */
class ClazzLoader<T>(c: Class<T>) {

    val c: Class<T>

    init{
        this.c = c
    }

    fun getClazz() : Class<T> {
        return c
    }

}