package com.rotor.notifications.interfaces

import com.rotor.notifications.model.Notification

/**
 * Created by efraespada on 25/03/2018.
 */
interface Listener {

    fun opened(deviceId: String, notification: Notification)

    fun removed(notification: Notification)

}