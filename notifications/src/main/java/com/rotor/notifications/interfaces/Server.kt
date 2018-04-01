package com.rotor.notifications.interfaces

import com.rotor.database.request.*
import com.rotor.notifications.request.NotificationSender
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by efraespada on 14/03/2018.
 */
interface Server {

    @Headers("Content-Type: application/json")
    @POST("/")
    fun sendNotification(@Body notificationSender: NotificationSender) : Observable<SyncResponse>

}