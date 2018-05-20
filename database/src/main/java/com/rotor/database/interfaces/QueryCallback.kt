package com.rotor.database.interfaces

import org.json.JSONArray

interface QueryCallback {

    fun response(list: JSONArray)

}