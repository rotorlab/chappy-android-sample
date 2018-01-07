package com.flamebase.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.flamebase.database.model.CallbackIO;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

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
                if (args.length == 2) {
                    Object o = args[args.length - 1];
                    if (o instanceof Ack) {
                        Ack ack = (Ack) args[args.length - 1];
                        ack.call();
                    }

                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            callbackIO.received((JSONObject) args[0]);
                        }
                    };
                    new Handler(Looper.getMainLooper()).post(task);
                } else if (args.length == 1) {
                    Object o = args[0];
                    if (o instanceof Ack) {
                        Ack ack = (Ack) args[0];
                        ack.call();
                    } else if (o instanceof JSONObject) {
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                callbackIO.received((JSONObject) args[0]);
                            }
                        };
                        new Handler(Looper.getMainLooper()).post(task);
                    }
                }

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
            IO.Options opts = new IO.Options();
            opts.reconnection = true;
            mSocket = IO.socket(url, opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (mSocket != null) {
            mSocket.on(key, onNewMessage);
            mSocket.io().timeout(-1);
            if (!mSocket.connected()) {
                Log.d(FlamebaseDatabase.class.getSimpleName(), "connecting socket");
                mSocket.connect();
            }
        }

        return mSocket;
    }


}
