package com.flamebase.database;

import android.content.Context;
import android.util.Log;

import com.flamebase.database.interfaces.Blower;
import com.flamebase.database.interfaces.ListBlower;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;
import com.flamebase.database.model.MapReference;
import com.flamebase.database.model.ObjectReference;
import com.flamebase.database.model.Reference;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.flamebase.database.model.Reference.PATH;

/**
 * Created by efraespada on 10/06/2017.
 */

public class FlamebaseDatabase {

    private static final String TAG = FlamebaseDatabase.class.getSimpleName();

    private static Context context;
    private static String urlServer;
    private static String token;
    private static Gson gson;

    private static HashMap<String, Reference> pathMap;

    public enum Type {
        OBJECT,
        LIST,
        MAP
    }

    public interface FlamebaseReference<T> {

        void onObjectChanges(T value);

        T update();

        void progress(String id, int value);

        String getTag();
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
        FlamebaseDatabase.gson = new Gson();
        if (FlamebaseDatabase.pathMap == null) {
            FlamebaseDatabase.pathMap = new HashMap<>();
        }
    }

    /*
     * Creates a new RealtimeDatabase stringReference
     * @param path                  - Database stringReference path
     * @param flamebaseReference    - Callback methods
     *
    public static <T> void createListener(final String path, final FlamebaseReference flamebaseReference, RealtimeDatabase.Blower blower) {
        if (FlamebaseDatabase.pathMap == null) {
            Log.e(TAG, "Use FlamebaseDatabase.initialize(Context context, String urlServer) before create real time references");
            return;
        }

        final RealtimeDatabase realtimeDatabase = new RealtimeDatabase(FlamebaseDatabase.context, path, blower) {

            @Override
            public void progress(String id, int value) {
                flamebaseReference.progress(id, value);
            }

            @Override
            public String getTag() {
                return flamebaseReference.getTag();
            }
        };

        FlamebaseDatabase.pathMap.put(path, realtimeDatabase);

        realtimeDatabase.loadChachedReference();

        FlamebaseDatabase.initSync(path, FlamebaseDatabase.token, new Sender.FlamebaseResponse() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (jsonObject.has("data") && jsonObject.get("data") != null) {
                        Object object = jsonObject.get("data");
                        if (object instanceof JSONObject) {
                            JSONObject obj = (JSONObject) object;
                            int len = obj.getInt("len");

                            if (realtimeDatabase.len > len) {
                                Log.e(TAG, "not up to date : " + path);
                                syncReference(path, true);
                            }

                        } else {
                            Log.e(TAG, "action success: " + object);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }
     */

    public static <T> void createListener(final String path, Blower<T> blower, Class<T> clazz) {
        if (FlamebaseDatabase.pathMap == null) {
            Log.e(TAG, "Use FlamebaseDatabase.initialize(Context context, String urlServer) before create real time references");
            return;
        }

        // clazz of element

        Type type;
        if (blower instanceof MapBlower) {
            type = Type.MAP;
        } else if (blower instanceof ListBlower) {
            type = Type.LIST;
        } else {
            type = Type.OBJECT;
        }


        switch (type) {

            case MAP:

                final MapBlower<T> mapBlower = (MapBlower<T>) blower;

                final MapReference mapReference = new MapReference<T>(context, path, mapBlower, clazz) {

                    @Override
                    public void progress(String id, int value) {
                        mapBlower.progress(id, value);
                    }

                    @Override
                    public String getTag() {
                        return mapBlower.getTag();
                    }

                    @Override
                    public Map<String, T> updateMap() {
                        return mapBlower.updateMap();
                    }

                };

                pathMap.put(path, mapReference);

                mapReference.loadCachedReference();

                FlamebaseDatabase.initSync(path, FlamebaseDatabase.token, new Sender.FlamebaseResponse() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        try {
                            if (jsonObject.has("data") && jsonObject.get("data") != null) {
                                Object object = jsonObject.get("data");
                                if (object instanceof JSONObject) {
                                    JSONObject obj = (JSONObject) object;
                                    int len = obj.getInt("len");

                                    if (mapReference.getStringReference().length() > len) {
                                        Log.e(TAG, "not up to date : " + path);
                                        syncReference(path, true);
                                    }

                                } else {
                                    Log.e(TAG, "action success: " + object);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "error: " + error);
                    }
                });

                break;

            case OBJECT:

                final ObjectBlower<T> objectBlower = (ObjectBlower<T>) blower;

                final ObjectReference objectReference = new ObjectReference<T>(context, path, objectBlower, clazz) {


                    @Override
                    public T updateObject() {
                        return objectBlower.updateObject();
                    }

                    @Override
                    public void progress(String id, int value) {
                        objectBlower.progress(id, value);
                    }

                    @Override
                    public String getTag() {
                        return objectBlower.getTag();
                    }

                };

                pathMap.put(path, objectReference);

                objectReference.loadCachedReference();

                FlamebaseDatabase.initSync(path, FlamebaseDatabase.token, new Sender.FlamebaseResponse() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        try {
                            if (jsonObject.has("data") && jsonObject.get("data") != null) {
                                Object object = jsonObject.get("data");
                                if (object instanceof JSONObject) {
                                    JSONObject obj = (JSONObject) object;
                                    int len = obj.getInt("len");

                                    if (objectReference.getStringReference().length() > len) {
                                        Log.e(TAG, "not up to date : " + path);
                                        syncReference(path, true);
                                    }

                                } else {
                                    Log.e(TAG, "action success: " + object);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });

                break;
        }
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

    private static void sendUpdate(final String path, String differences, int len) {
        try {
            JSONObject map = new JSONObject();
            map.put("method", "update_data");
            map.put("path", path);
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
                                            try {
                                                if (jsonObject.has("data") && jsonObject.get("data") != null) {
                                                    Object object = jsonObject.get("data");
                                                    if (object instanceof JSONObject) {
                                                        JSONObject obj = (JSONObject) object;
                                                        String info = obj.getString("info");
                                                        int len = obj.getInt("len");

                                                        syncReference(path, true);
                                                    } else {
                                                        Log.e(TAG, "action success: " + object);
                                                    }
                                                }
                                                Log.e(TAG, "action success: " + jsonObject.toString());

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            Log.e(TAG, error);
                                        }
                                    });
                                    break;

                                case "inconsistency_length":
                                    syncReference(path, true);
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
            String path = remoteMessage.getData().get(PATH);
            if (pathMap.containsKey(path)) {
                ((Reference) pathMap.get(path)).onMessageReceived(remoteMessage);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static <T> void syncReference(String path, boolean clean) {
        if (pathMap.containsKey(path)) {
            Object[] result = pathMap.get(path).syncReference(clean);
            String diff = (String) result[1];
            int len = (int) result[0];
            sendUpdate(path, diff, len);
        }
    }
}
