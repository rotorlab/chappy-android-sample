package com.rotor.database.interfaces

import com.rotor.database.request.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by efraespada on 14/03/2018.
 */
interface Server {

    @Headers("Content-Type: application/json")
    @POST("/")
    fun createReference(@Body createListener: CreateListener): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun removeListener(@Body removeListener: RemoveListener): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun refreshFromServer(@Body updateFromServer: UpdateFromServer): Call<SyncResponse>

    @Headers("Content-Type: application/json")
    @POST("/")
    fun refreshToServer(@Body updateToServer: UpdateToServer): Call<SyncResponse>

}