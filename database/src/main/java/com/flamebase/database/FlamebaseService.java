package com.flamebase.database;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

/**
 * Created by efrainespada on 20/02/2018.
 */

public class FlamebaseService extends Service {

    private static final String TAG = FlamebaseService.class.getSimpleName();
    private final IBinder binder = new FlamebaseService.FBinder();
    private static boolean initialized;
    private static RedisClient client;
    private static RedisPubSubCommands<String, String> connection;
    private ServiceConnection sc;

    @Override
    public void onCreate() {
        super.onCreate();
        client = RedisClient.create(FlamebaseDatabase.urlRedis);
        connection = client.connectPubSub().sync();
        connection.getStatefulConnection().addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String channel, String message) {
                try {
                    FlamebaseDatabase.onMessageReceived(new JSONObject(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void message(String pattern, String channel, String message) {
                // nothing to do here
            }

            @Override
            public void subscribed(String channel, long count) {
                // nothing to do here
            }

            @Override
            public void psubscribed(String pattern, long count) {
                // nothing to do here
            }

            @Override
            public void unsubscribed(String channel, long count) {
                // nothing to do here
            }

            @Override
            public void punsubscribed(String pattern, long count) {
                // nothing to do here
            }
        });
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
            connection.subscribe(FlamebaseDatabase.id);
        }
    }

    public void stopService() {
        if (initialized) {
            initialized = false;
            if (connection != null) {
                connection.unsubscribe(FlamebaseDatabase.id);
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

}
