package com.flamebase.database;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


/**
 * Created by efrainespada on 20/02/2018.
 */

public class FlamebaseService extends Service {

    private static final String TAG = FlamebaseService.class.getSimpleName();
    private final IBinder binder = new FlamebaseService.FBinder();
    private static boolean initialized;
    private static RedisClient client;
    private static Long moment;
    private static RedisPubSubConnection<String, String> connection;
    private ServiceConnection sc;
    private FlamebaseDatabase.InternalServiceListener listener;
    private boolean connectedToRedis;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        client = RedisClient.create(FlamebaseDatabase.urlRedis);
        connection = client.connectPubSub();
        connection.addListener(new RedisPubSubListener<String, String>() {
            @Override
            public void message(String s, final String s2) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FlamebaseDatabase.onMessageReceived(new JSONObject(s2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                new Handler(getApplicationContext().getMainLooper()).post(task);
            }

            @Override
            public void message(String s, String k1, String s2) {
                // nothing to do here
            }

            @Override
            public void subscribed(String s, long l) {
                moment = new Date().getTime();
                connectedToRedis = true;
                if (listener != null) {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            listener.connected();
                        }
                    };
                    new Handler(getApplicationContext().getMainLooper()).post(task);
                }
            }

            @Override
            public void psubscribed(String s, long l) {
                // nothing to do here
            }

            @Override
            public void unsubscribed(String s, long l) {
                moment = null;
                connectedToRedis = false;
                if (listener != null) {
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            listener.connected();
                        }
                    };
                    new Handler(getApplicationContext().getMainLooper()).post(task);
                }
            }

            @Override
            public void punsubscribed(String s, long l) {
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
            if (client != null && connection != null) {
                connection.subscribe(FlamebaseDatabase.id);
            }
        } else if (connectedToRedis) {
            if (listener != null) {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        listener.connected();
                    }
                };
                new Handler(getApplicationContext().getMainLooper()).post(task);
            }

        }
    }

    public void stopService() {
        if (initialized) {
            initialized = false;
            if (client != null && connection != null) {
                connection.unsubscribe(FlamebaseDatabase.id);
            }
        }
    }

    public void setServiceConnection(ServiceConnection sc) {
        Log.d(TAG, "serviceConnection set");
        this.sc = sc;
    }

    public void setListener(FlamebaseDatabase.InternalServiceListener listener) {
        this.listener = listener;
        if (initialized) {
            this.listener.connected();
        }
    }

    public Long getMoment() {
        return moment;
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
