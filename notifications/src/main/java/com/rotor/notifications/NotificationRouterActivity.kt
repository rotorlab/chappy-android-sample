package com.rotor.notifications

import android.content.Intent
import android.support.v7.app.AppCompatActivity

/**
 * Created by efraespada on 23/03/2018.
 */
abstract class NotificationRouterActivity : AppCompatActivity() {

    abstract fun notificationTouched(notification: Int)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        notificationTouched(requestCode)
    }

}