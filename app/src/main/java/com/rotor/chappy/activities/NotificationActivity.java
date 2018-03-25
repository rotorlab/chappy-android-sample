package com.rotor.chappy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.rotor.chappy.BuildConfig;
import com.rotor.chappy.ContactsListener;
import com.rotor.chappy.services.ChatManager;
import com.rotor.chappy.services.LocalData;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.StatusListener;
import com.rotor.database.Database;
import com.rotor.notifications.NotificationRouterActivity;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.interfaces.Listener;
import com.rotor.notifications.model.Content;
import com.rotor.notifications.model.Notification;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

/**
 * Created by efraespada on 23/03/2018.
 */

public class NotificationActivity extends NotificationRouterActivity {

    @Override
    public void onCreate(@org.jetbrains.annotations.Nullable Bundle savedInstanceState, int action, @NotNull String id, @NotNull String data) {
        LocalData.init(getApplicationContext());

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void connected() {
                Database.initialize();
                ChatManager.splashSyncContacts(new ContactsListener() {
                    @Override
                    public void contactsReady() {
                        JSONArray array = LocalData.getLocalPaths();
                        for (int i = 0; i < array.length(); i++) {
                            try {
                                final String path = array.getString(i);
                                ChatManager.addGChat(path, new ChatManager.CreateChatListener() {
                                    @Override
                                    public void newChat() {
                                        Database.unlisten(path);
                                        LocalData.removePath(path);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
                Notifications.initialize(NotificationActivity.class, new Listener() {
                    @Override
                    public void opened(@NotNull String deviceId, @NotNull Notification notification) {

                    }

                    @Override
                    public void removed(@NotNull Notification notification) {

                    }
                });
            }

            @Override
            public void reconnecting() {

            }
        });
        Rotor.debug(true);
    }

    @Override
    public void notificationTouched(int action, @NotNull String id, @NotNull String data) {
        if (action == SplashActivity.ACTION_CHAT) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("path", data);
            intent.putExtra("notification", id);
            startActivity(intent);
        }

        finish();
    }

}
