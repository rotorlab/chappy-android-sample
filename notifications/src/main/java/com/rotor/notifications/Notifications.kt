package com.rotor.notifications

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
import android.util.Log
import com.google.gson.Gson
import com.rotor.database.utils.ReferenceUtils
import com.rotor.notifications.data.NotificationDocker
import com.rotor.notifications.interfaces.Server
import com.rotor.notifications.request.NotificationSender
import com.stringcare.library.SC
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList


/**
 * Created by efraespada on 18/03/2018.
 */
class Notifications {

    companion object {

        val NOTIFICATION = "/notifications/"
        private  var docker: NotificationDocker? = null
        private val TAG: String = Notifications::class.java.simpleName!!

        val api by lazy {
            service(Rotor.urlServer!!)
        }

        @JvmStatic fun initialize() {
            SC.init(Rotor.context)

            loadCachedNotifications()

            val config = ImageLoaderConfiguration.Builder(Rotor.context).build()
            ImageLoader.getInstance().init(config)


            Rotor.prepare(Builder.NOTIFICATION, object: BuilderFace {
                override fun onMessageReceived(jsonObject: JSONObject) {
                    try {
                        if (jsonObject.has("notifications")) {
                            val notification = jsonObject.getJSONObject("notifications")
                            if (notification.has("method") && notification.has("id")) {
                                val method = notification.getString("method")
                                if (Method.ADD.getMethod().equals(method)) {
                                    if (!docker!!.notifications!!.containsKey(notification.getString("id"))) {
                                        createNotification(notification.getString("id"), null)
                                    }
                                } else if (Method.REMOVE.getMethod().equals(method)) {
                                    if (docker!!.notifications!!.containsKey(notification.getString("id"))) {
                                        removeNotification(NOTIFICATION + notification.getString("id"))
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

        private fun service(url: String): Server {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build()

            return retrofit.create(Server::class.java)
        }

        @JvmStatic fun builder(content: Content ?, data: Data ?, receivers: List<String>) : Notification {
            val id = Date().time
            val map = HashMap<String, Receiver>()
            for (receiver in receivers) {
                map[receiver] = Receiver(receiver, null)
            }
            return Notification(id.toString(), id, content, data, Sender(Rotor.id!!, id), map)
        }

        @JvmStatic fun createNotification(id: String, notificationn: Notification ?) {
            var identifier = if (!id.contains("notifications")) NOTIFICATION + id else id
            if (!docker!!.notifications!!.containsKey(identifier)) {
                Database.listen(identifier, object: Reference<Notification>(Notification::class.java) {

                    var created = false

                    override fun onCreate() {
                        notificationn?.let {
                            docker!!.notifications!![identifier] = notificationn
                            Database.sync(identifier)
                            created = true
                        }
                    }

                    override fun onChanged(ref: Notification) {
                        docker!!.notifications!![identifier] = ref
                        val gson = Gson()
                        val notificationsAsString = gson.toJson(docker!!)
                        ReferenceUtils.addElement(NOTIFICATION, notificationsAsString)
                        showNotification(identifier)
                        if (created) {
                            created = false
                            var rece: ArrayList<Receiver> = arrayListOf()
                            rece.addAll(ref.receivers.values)
                            sendNotification(identifier, rece)
                        }
                    }

                    override fun onUpdate(): Notification ? {
                        if (docker!!.notifications!!.containsKey(identifier)) {
                            return docker!!.notifications!!.get(identifier)
                        } else {
                            return null
                        }
                    }

                    override fun progress(value: Int) {
                        // nothing to do here
                    }

                    override fun onDestroy() {
                        if (docker!!.notifications!!.containsKey(identifier)) {
                            docker!!.notifications!!.remove(identifier)
                        }
                    }

                })
            }
        }

        @JvmStatic fun removeNotification(id: String) {
            var identifier = NOTIFICATION + id
            if (docker!!.notifications!!.containsKey(identifier)) {
                docker!!.notifications!!.remove(identifier)
                Database.unlisten(identifier)
            }
        }

        @JvmStatic fun showNotification(id: String) {
            if (docker!!.notifications!!.containsKey(id)) {
                val notification = docker!!.notifications!![id]

                val content = notification!!.content

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

        private fun interShowNotification(id: String, content: Content, bitmap: Bitmap ?) {
            var mBuilder: NotificationCompat.Builder ? = null
            if (bitmap != null) {
                mBuilder = NotificationCompat.Builder(Rotor.context!!, id)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setLargeIcon(bitmap)
                        .setContentTitle(content.title)
                        .setContentText(content.body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            } else {
                mBuilder = NotificationCompat.Builder(Rotor.context!!, id)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(content.title)
                        .setContentText(content.body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (content.channel != null && content.channelDescription != null) {
                    val name = content.channel
                    val description = content.channelDescription
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    var channel: NotificationChannel ? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        channel = NotificationChannel(id, name, importance)
                    }
                    channel!!.description = description

                    val notificationManager = Rotor.context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
                    if (notificationManager.areNotificationsEnabled()) {
                        var found = false
                        for (channels in notificationManager.notificationChannels) {
                            if (channels.id.equals(id)) {
                                found = true
                                break
                            }
                        }
                        if (!found) {
                            notificationManager.createNotificationChannel(channel)
                        }
                    }
                } else {
                    return
                }
            }

            val notificationManager = NotificationManagerCompat.from(Rotor.context!!)
            val idNumber = id.split("/")[2].toLong()
            notificationManager.notify(idNumber.toInt(), mBuilder.build())
        }

        @JvmStatic fun loadCachedNotifications() {
            val notificationsAsString: String ? = ReferenceUtils.getElement(NOTIFICATION)
            if (notificationsAsString == null) {
                docker = NotificationDocker()
            } else {
                val gson = Gson()
                docker = gson.fromJson(notificationsAsString, NotificationDocker::class.java) as NotificationDocker
                val id = Rotor.id
                for (notification in docker!!.notifications!!.values) {
                    if (!id.equals(notification.sender.id)) {
                        for (receiver in notification.receivers.values) {
                            if (receiver.id.equals(id) && receiver.viewed == null) {
                                showNotification(NOTIFICATION + notification.id)
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic private fun sendNotification(id: String, receivers: ArrayList<Receiver>) {
            api.sendNotification(NotificationSender("send_notifications", id, receivers))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                result.status?.let {
                                    Log.e(TAG, result.status)
                                }
                            },
                            { error -> error.printStackTrace() }
                    )
        }

    }
}

