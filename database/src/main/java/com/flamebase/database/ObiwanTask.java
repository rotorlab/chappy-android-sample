package com.flamebase.database;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by efraespada on 22/02/2018.
 */

public class ObiwanTask implements Runnable {

    private static JedisPubSub listener;
    private static Jedis jedis;
    private boolean connectedToRedis;

    @Override
    public void run() {
        jedis = new Jedis(FlamebaseDatabase.urlRedis);
        listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, final String message) {
                super.onMessage(channel, message);
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("TEEEEEEEEEEEEEEST", message);
                            FlamebaseDatabase.onMessageReceived(new JSONObject(message));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Handler(Looper.getMainLooper()).post(task);
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                super.onSubscribe(channel, subscribedChannels);
                connectedToRedis = true;
                FlamebaseDatabase.statusListener.ready();
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                super.onUnsubscribe(channel, subscribedChannels);
                connectedToRedis = false;
            }
        };

    }

    public void startService() {
        if (jedis != null && listener != null) {
            try {
                jedis.subscribe(listener, FlamebaseDatabase.urlRedis);
            } catch (Exception e) {
                Log.e("test", ">>> OH NOES Sub - " + e.getMessage());
                // e.printStackTrace();
            }
        }
    }

    public void stopService() {
        if (jedis != null && listener != null) {
            listener.unsubscribe(FlamebaseDatabase.id);
        }
    }
}
