package com.flamebase.chat.services;

import android.util.Log;

import com.flamebase.database.FlamebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by efraespada on 04/06/2017.
 */

public class FMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("TESTA", remoteMessage.getData().toString());
        FlamebaseDatabase.onMessageReceived(remoteMessage);
    }
}
