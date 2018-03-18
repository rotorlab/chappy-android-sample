package com.rotor.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.app.NotificationCompat
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.rotor.core.Builder
import com.rotor.core.Rotor
import com.rotor.core.interfaces.BuilderFace
import com.rotor.database.Database
import com.rotor.database.abstr.Reference
import com.rotor.notifications.enums.Method
import com.rotor.notifications.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import android.graphics.Bitmap
import android.view.View
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build





/**
 * Created by efraespada on 18/03/2018.
 */
class Notifications {

    companion object {

        val NOTIFICATION = "notifications/"
        private  var context: Context ? = null
        private var notifications: HashMap<String, Notification> ? = null

        @JvmStatic fun initialize(context: Context) {
            Notifications.Companion.context = context
            notifications.let {
                notifications = HashMap()
            }

            val config = ImageLoaderConfiguration.Builder(context).build()
            ImageLoader.getInstance().init(config)

            Rotor.prepare(Builder.NOTIFICATION, object: BuilderFace {
                override fun onMessageReceived(jsonObject: JSONObject) {
                    try {
                        if (jsonObject.has("notifications")) {
                            val notificationMap = jsonObject.get("notifications") as JSONArray
                            for (i in 0..(notificationMap.length() - 1)) {
                                val notification = notificationMap.getJSONObject(i)
                                if (notification.has("method") && notification.has("id")) {
                                    val method = notification.getString("method")
                                    if (Method.ADD.getMethod().equals(method)) {
                                        if (!notifications!!.containsKey(notification.getString("id"))) {
                                            createNotification(NOTIFICATION + notification.getString("id"), null)
                                        }
                                    } else if (Method.REMOVE.getMethod().equals(method)) {

                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        }

        fun builder(content: Content ?, data: Data ?, receivers: List<String>) : Notification {
            val id = Date().time
            val map = HashMap<String, Receiver>()
            for (receiver in receivers) {
                map[receiver] = Receiver(receiver, null)
            }
            return Notification(id = id.toString(), time = id, content = content, data = data, receivers = map, sender = Sender(Rotor.id!!, id))
        }

        fun createNotification(id: String, notification: Notification ?) {
            if (!notifications!!.containsKey(id)) {
                Database.listen(id, object: Reference<Notification>(Notification::class.java) {

                    override fun onCreate() {
                        notification?.let {
                            Notifications.Companion.notifications!![notification.id] = notification
                            Database.sync(id)
                        }
                    }

                    override fun onChanged(ref: Notification) {
                        Notifications.Companion.notifications!![ref.id] = ref
                    }

                    override fun onUpdate(): Notification ? {
                        if (Notifications.Companion.notifications!!.containsKey(id)) {
                            return Notifications.Companion.notifications!!.get(id)
                        } else {
                            return null
                        }
                    }

                    override fun progress(value: Int) {
                        // nothing to do here
                    }

                })
            }
        }

        fun removeNotification(id: String) {
            if (notifications!!.containsKey(id)) {

                Database.unlisten(id)
            }
        }

        private fun showNotification(id: String) {
            if (notifications!!.containsKey(id)) {
                val notification = notifications!![id]

                val content = notification!!.content
                val data = notification!!.data


                content?.let {
                    if (content.photo != null) {
                        val imageLoader = ImageLoader.getInstance()
                        imageLoader.loadImage(content.photo, object : SimpleImageLoadingListener() {
                            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                                interShowNotification(id, content, loadedImage)
                            }
                        })
                    } else {
                        interShowNotification(id, content, null)
                    }
                }
            }
        }

        @SuppressLint("NewApi")
        private fun interShowNotification(id: String, content: Content, bitmap: Bitmap ?) {
            var mBuilder: NotificationCompat.Builder ? = null
            if (bitmap != null) {
                mBuilder = NotificationCompat.Builder(Notifications.Companion.context!!, id)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setLargeIcon(bitmap)
                        .setContentTitle(content.title)
                        .setContentText(content.body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            } else {
                mBuilder = NotificationCompat.Builder(Notifications.Companion.context!!, id)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(content.title)
                        .setContentText(content.body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && content.channel != null && content.channelDescription != null) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                val name = content.channel
                val description = content.channelDescription
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(id, name, importance)
                channel.description = description
                // Register the channel with the system
                val notificationManager = Notifications.Companion.context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.createNotificationChannel(channel)
                }
            } else {
                val notificationManager = NotificationManagerCompat.from(Notifications.Companion.context!!)
                notificationManager.notify(id.toInt(), mBuilder.build())
            }
        }


    }
}