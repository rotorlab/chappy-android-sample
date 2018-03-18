package com.rotor.notifications.model

/**
 * Created by efraespada on 18/03/2018.
 */
data class Notification( val id: String,
                         val time: Long,
                         var content: Content ?,
                         var data: Data ?,
                         var sender: Sender,
                         var receivers: HashMap<String, Receiver>)