package com.rotor.chappy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rotor.chappy.BuildConfig;
import com.rotor.chappy.ContactsListener;
import com.rotor.chappy.services.ChatManager;
import com.rotor.chappy.services.LocalData;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.StatusListener;
import com.rotor.database.Database;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.model.Content;
import com.rotor.notifications.model.Notification;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by efraespada on 27/02/2018.
 */

public class SplashActivity extends AppCompatActivity {

    public static int ACTION_CHAT = 4532;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalData.init(getApplicationContext());

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void connected() {
                Database.initialize();

                Notifications.initialize(NotificationActivity.class);

                Content content = new Content(ACTION_CHAT,
                        "Hi :)",
                        "Welcome to notifications!",
                        "ttt",
                        "myChannel",
                        "Test channel",
                        null,
                        null);

                ArrayList<String> ids = new ArrayList<>();
                ids.add(Rotor.getId());
                ids.add("f33f3642e39650b9");
                ids.add("48484aad18e02d76");

                Notification notification = Notifications.builder(content, ids);

                Notifications.createNotification(notification.getId(), notification);

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
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void reconnecting() {

            }
        });
        Rotor.debug(true);
    }
}
