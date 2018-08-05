package com.rotor.chappy.activities.notifications;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.rotor.chappy.BuildConfig;
import com.rotor.chappy.activities.splash.SplashActivity;
import com.rotor.chappy.activities.chat.ChatActivity;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.RStatus;
import com.rotor.database.Database;
import com.rotor.notifications.NotificationRouterActivity;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.interfaces.Listener;
import com.rotor.notifications.model.Notification;

/**
 * Created by efraespada on 23/03/2018.
 */

public class NotificationActivity extends NotificationRouterActivity {

    public static String TAG = NotificationActivity.class.getSimpleName();

    @Override
    public void onCreate() {
        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new RStatus() {
            @Override
            public void ready() {
                Database.initialize();
                Notifications.initialize(NotificationActivity.class, new Listener() {
                    @Override
                    public void opened(@NonNull String deviceId, @NonNull Notification notification) {
                        Toast.makeText(getApplicationContext(), deviceId + " opened \"" + notification.getContent().getBody() + "\"", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void removed(@NonNull Notification notification) {

                    }
                });
            }
        });
        Rotor.debug(true);
    }

    @Override
    public void notificationTouched(int action, @NonNull String id, @NonNull String room) {
        if (action == SplashActivity.ACTION_CHAT) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("path", room);
            intent.putExtra("notification", id);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }
}
