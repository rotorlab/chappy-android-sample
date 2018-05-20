package com.rotor.database.request

/**
 * Created by efraespada on 19/05/2018.
 */

data class Query(val database: String,
                 val path: String,
                 val query: String,
                 val mask: String)