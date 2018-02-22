package com.flamebase.database;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import redis.client.RedisClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by efrainespada on 20/02/2018.
 */

public class FlamebaseService extends Service {

    private static final String TAG = FlamebaseService.class.getSimpleName();
    private final IBinder binder = new FlamebaseService.FBinder();
    private static boolean initialized;
    private static JedisPubSub listener;
    private static Jedis jedis;
    private ServiceConnection sc;
    private boolean connectedToRedis;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            RedisClient client = new RedisClient(FlamebaseDatabase.urlRedis, 6379);
            client.subscribe();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jedis = new Jedis(FlamebaseDatabase.urlRedis);
        listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                super.onMessage(channel, message);
                try {
                    FlamebaseDatabase.onMessageReceived(new JSONObject(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                super.onSubscribe(channel, subscribedChannels);
                connectedToRedis = true;

                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        FlamebaseDatabase.statusListener.ready();
                    }
                };
                new Handler(Looper.getMainLooper()).post(task);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                super.onUnsubscribe(channel, subscribedChannels);
                connectedToRedis = false;
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service start");
        startService();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "service bound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service destroyed");
        super.onDestroy();
    }

    public void startService() {
        if (!initialized) {
            initialized = true;
            if (jedis != null && listener != null) {
                jedis.subscribe(listener, FlamebaseDatabase.urlRedis);
            }
        }
    }

    public void stopService() {
        if (initialized) {
            initialized = false;
            if (jedis != null && listener != null) {
                listener.unsubscribe(FlamebaseDatabase.id);
            }
        }
    }

    public void setServiceConnection(ServiceConnection sc) {
        Log.d(TAG, "serviceConnection set");
        this.sc = sc;
    }

    public ServiceConnection getServiceConnection() {
        return this.sc;
    }

    public class FBinder extends Binder {

        public FlamebaseService getService() {
            return FlamebaseService.this;
        }

    }

    public class AsyncSub extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (jedis != null && listener != null) {
                jedis.subscribe(listener, FlamebaseDatabase.urlRedis);
            }
            return null;
        }
    }

    public class AsyncUnSub extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (jedis != null && listener != null) {
                jedis.subscribe(listener, FlamebaseDatabase.urlRedis);
            }
            return null;
        }
    }

}
