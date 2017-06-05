package com.flamebase.chat.services;

import android.content.Context;

import com.flamebase.database.RealtimeDatabase;
import com.flamebase.chat.model.GChat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by efraespada on 04/06/2017.
 */

public class FMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        refreshGroupChat(getApplicationContext(), remoteMessage);
    }

    public void refreshGroupChat(Context context, RemoteMessage remoteMessage) {

        new RealtimeDatabase<GChat>(context, GChat.class, remoteMessage) {

            @Override
            public void onObjectChanges(final GChat value) {

            }

            @Override
            public void progress(String id, int value) {

            }

            @Override
            public String getTag() {
                return "chat_sync";
            }
        };
    }
}
