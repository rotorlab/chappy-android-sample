package com.rotor.database.interfaces

interface QueryCallback<in T> {

    fun response(response: T)

}