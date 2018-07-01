package com.rotor.notifications

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.rotor.core.RAppCompatActivity
import com.rotor.core.Rotor

/**
 * Created by efraespada on 23/03/2018.
 */
abstract class NotificationRouterActivity : RAppCompatActivity() {

    var enter: Boolean ? = false

    interface NotificationsStatus {
        fun ready()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enter = true
        var requestCode : Int ? = null
        var id : String ? = null
        var room : String ? = null
        if (intent.hasExtra(Notifications.ID)) {
            id = intent.getStringExtra(Notifications.ID)
            if (id == null){
                id = ""
            }
        }
        if (intent.hasExtra(Notifications.RC)) {
            requestCode = intent.getIntExtra(Notifications.RC, 0)
        }
        if (intent.hasExtra(Notifications.ROOM)) {
            room = intent.getStringExtra(Notifications.ROOM)
            if (room == null){
                room = ""
            }
        }

        Notifications.listener(object : NotificationsStatus {
            override fun ready() {
                if (enter != null && enter as Boolean) {
                    notificationTouched(requestCode!!, id!!, room!!)
                }
                enter = false
            }
        })

        onCreate()
    }

    abstract fun notificationTouched(action: Int, id: String, room: String)

    abstract fun onCreate()

}