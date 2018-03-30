package com.rotor.notifications

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by efraespada on 23/03/2018.
 */
abstract class NotificationRouterActivity : AppCompatActivity() {

    interface NotificationsStatus {
        fun ready()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var requestCode : Int ? = null
        var id : String ? = null
        var data : String ? = null
        if (intent.hasExtra(Notifications.ID)) {
            id = intent.getStringExtra(Notifications.ID)
            if (id == null){
                id = ""
            }
        }
        if (intent.hasExtra(Notifications.RC)) {
            requestCode = intent.getIntExtra(Notifications.RC, 0)
        }
        if (intent.hasExtra(Notifications.DATA)) {
            data = intent.getStringExtra(Notifications.DATA)
            if (data == null){
                data = ""
            }
        }

        Notifications.listener(object : NotificationsStatus {
            override fun ready() {
                notificationTouched(requestCode!!, id!!, data!!)
            }
        })

        onCreate()
    }

    abstract fun notificationTouched(action: Int, id: String, data: String)

    abstract fun onCreate()

}