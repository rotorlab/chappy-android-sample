package com.rotor.notifications.request

import com.rotor.notifications.model.Receiver

/**
 * Created by efraespada on 11/03/2018.
 */

data class NotificationSender(val method: String, val id: String, val receivers: ArrayList<Receiver>)