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
import com.rotor.notifications.model.Content;
import com.rotor.notifications.model.Notification;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by efraespada on 23/03/2018.
 */

public class NotificationActivity extends NotificationRouterActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalData.init(getApplicationContext());

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void connected() {
                Database.initialize();
                Notifications.initialize(NotificationActivity.class);
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
            intent.putExtra("path", data);
            intent.putExtra("notification", id);
            startActivity(intent);
        }

        finish();
    }
}
