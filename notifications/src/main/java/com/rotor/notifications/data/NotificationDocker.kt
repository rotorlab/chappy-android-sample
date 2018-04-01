package com.rotor.notifications.data

import com.rotor.notifications.model.Notification

/**
 * Created by efraespada on 18/03/2018.
 */
class NotificationDocker() {

    var notifications: HashMap<String, Notification> ? = null

    init {
        notifications = HashMap()
    }

}