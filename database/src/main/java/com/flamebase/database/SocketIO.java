package com.flamebase.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.flamebase.database.model.CallbackIO;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by efraespada on 03/01/2018.
 */

public class SocketIO {

    public static void init(Context context) {

    }

    public static Socket getInstance(String url, String key, final CallbackIO callbackIO) {
        Socket mSocket = null;

        Emitter.Listener onNewMessage = new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        callbackIO.received((JSONObject) args[0]);
                    }
                };
                new Handler(Looper.getMainLooper()).post(task);

                /*
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                } catch (JSONException e) {
                    return;
                }
                */
            }
        };

        try {
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (mSocket != null) {
            mSocket.on(key, onNewMessage);
            mSocket.connect();
        }

        return mSocket;
    }


}
