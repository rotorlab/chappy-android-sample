package com.flamebase.database;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 10/06/2017.
 */

public class FlamebaseDatabase {

    private static final String TAG = FlamebaseDatabase.class.getSimpleName();

    private static Context context;
    private static String urlServer;
    private static String token;

    private static HashMap<String, RealtimeDatabase> pathMap;

    public interface FlamebaseReference<T> {

        void onObjectChanges(T value);

        T update();

        void progress(String id, int value);

        String getTag();

        Type getType();
    }

    private FlamebaseDatabase() {
        // nothing to do here
    }

    /**
     * Set initial config to sync with flamebase server cluster
     * @param context
     * @param urlServer
     */
    public static void initialize(Context context, String urlServer, String token) {
        FlamebaseDatabase.context = context;
        FlamebaseDatabase.urlServer = urlServer;
        FlamebaseDatabase.token = token;
        if (FlamebaseDatabase.pathMap == null) {
            FlamebaseDatabase.pathMap = new HashMap<>();
        }
    }

    /**
     * Creates a new RealtimeDatabase reference
     * @param path                  - Database reference path
     * @param flamebaseReference    - Callback methods
     */
    public static <T> void createListener(String path, final FlamebaseReference flamebaseReference) {
        if (FlamebaseDatabase.pathMap == null) {
            Log.e(TAG, "Use FlamebaseDatabase.initialize(Context context, String urlServer) before create real time references");
            return;
        }
        if (!FlamebaseDatabase.pathMap.containsKey(path)) {
            RealtimeDatabase realtimeDatabase = new RealtimeDatabase<T>(FlamebaseDatabase.context) {
                @Override
                public void onObjectChanges(T value) {
                    flamebaseReference.onObjectChanges(value);
                }

                @Override
                public T updateObject() {
                    return (T) flamebaseReference.update();
                }

                @Override
                public void progress(String id, int value) {
                    flamebaseReference.progress(id, value);
                }

                @Override
                public String getTag() {
                    return flamebaseReference.getTag();
                }

                @Override
                public Type getType() {
                    return flamebaseReference.getType();
                }
            };
            realtimeDatabase.loadChachedReference(path);
            FlamebaseDatabase.pathMap.put(path, realtimeDatabase);

        } else {
            // TODO check if should be necessary another post request message to refresh reference
        }

        FlamebaseDatabase.initSync(path, FlamebaseDatabase.token, new Sender.FlamebaseResponse() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    private static void initSync(String path, String token, final Sender.FlamebaseResponse callback) {
        try {
            JSONObject map = new JSONObject();
            map.put("method", "great_listener");
            map.put("path", path);
            map.put("token", token);
            map.put("os", "android");
            Sender.postRequest(FlamebaseDatabase.urlServer, map.toString(), new Sender.FlamebaseResponse() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    callback.onSuccess(jsonObject);
                    Log.e(TAG, "action success");
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                    Log.e(TAG, "action with error: " + error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void sendUpdate(final String database, final String path, String differences, int len) {
        try {
            JSONObject map = new JSONObject();
            map.put("method", "update_data");
            map.put("path", path);
            map.put("database", database);
            map.put("differences", differences);
            map.put("len", len);
            Sender.postRequest(FlamebaseDatabase.urlServer, map.toString(), new Sender.FlamebaseResponse() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    try {
                        if (jsonObject.has("error") && jsonObject.getString("error") != null){
                            String error = jsonObject.getString("error");
                            switch (error) {

                                case "holder_not_found":
                                    initSync(path, FlamebaseDatabase.token, new Sender.FlamebaseResponse() {
                                        @Override
                                        public void onSuccess(JSONObject jsonObject) {
                                            Log.e(TAG, jsonObject.toString());
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Log.e(TAG, error);
                                        }
                                    });
                                    break;

                                case "inconsistency_length":
                                    syncReference(database, path, true);
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "action success: " + jsonObject.toString());
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "action with error: " + error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String path = remoteMessage.getData().get(RealtimeDatabase.PATH);
            if (pathMap.containsKey(path)) {
                pathMap.get(path).onMessageReceived(remoteMessage);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static <T> void syncReference(String database, String path, boolean clean) {
        if (pathMap.containsKey(path)) {
            Object[] result = pathMap.get(path).syncReference(clean);
            String diff = (String) result[1];
            int len = (int) result[0];
            sendUpdate(database, path, diff, len);
        }
    }
}
