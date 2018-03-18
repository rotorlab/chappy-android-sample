package com.rotor.notifications

import com.rotor.notifications.model.Notification

/**
 * Created by efraespada on 18/03/2018.
 */
class Notifications {

    companion object {
        @JvmStatic var notifications: HashMap<String, Notification> ? = null

        @JvmStatic fun initialize() {
            notifications = HashMap<String, Notification>()
        }


    }
}