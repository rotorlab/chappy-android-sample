package com.flamebase.database;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.flamebase.database.interfaces.Blower;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;
import com.flamebase.database.interfaces.StatusListener;
import com.flamebase.database.interfaces.mods.KotlinMapBlower;
import com.flamebase.database.interfaces.mods.KotlinObjectBlower;
import com.flamebase.database.model.MapReference;
import com.flamebase.database.model.ObjectReference;
import com.flamebase.database.model.Reference;
import com.flamebase.database.model.request.CreateListener;
import com.flamebase.database.model.request.RemoveListener;
import com.flamebase.database.model.request.UpdateFromServer;
import com.flamebase.database.model.request.UpdateToServer;
import com.flamebase.database.model.service.SyncResponse;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.flamebase.database.model.Reference.EMPTY_OBJECT;
import static com.flamebase.database.model.Reference.PATH;
import static com.flamebase.database.model.Reference.ACTION_NEW_OBJECT;
import static com.flamebase.database.model.Reference.NULL;

/**
 * Created by efraespada on 10/06/2017.
 */

public class FlamebaseDatabase {

    private static final String TAG = FlamebaseDatabase.class.getSimpleName();

    private static final String KEY = "database";
    private static final String OS = "android";
    private static Context context;
    public static String id;
    private static String urlServer;
    public static String urlRedis;
    public static StatusListener statusListener;

    private static FlamebaseService flamebaseService;
    private static Boolean isServiceBound;

    private static Gson gson;
    public static Boolean debug;
    public static Boolean initialized;

    public static final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (service instanceof FlamebaseService.FBinder) {
                flamebaseService = ((FlamebaseService.FBinder) service).getService();
                flamebaseService.setServiceConnection(this);
                flamebaseService.setListener(new InternalServiceListener() {
                    @Override
                    public void connected() {
                        if (initialized) {
                            initialized = false;
                            statusListener.connected();
                        }
                    }

                    @Override
                    public void reconnecting() {
                        statusListener.reconnecting();
                    }
                });
                if (debug) Log.e(TAG, "instanced service");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals(FlamebaseService.class.getName())) {
                flamebaseService.setListener(null);
                flamebaseService = null;
            }
            if (debug) Log.e(TAG, "disconnected");
        }
    };

    private static HashMap<String, Reference> pathMap;

    public enum Type {
        OBJECT,
        MAP
    }

    interface InternalServiceListener {

        void connected();

        void reconnecting();

    }

    private FlamebaseDatabase() {
        // nothing to do here
    }

    /**
     * Set initial config to createReference with flamebase server cluster
     *
     * @param context
     * @param urlServer
     */
    public static void initialize(Context context, String urlServer, String redisServer, StatusListener statusListener) {
        FlamebaseDatabase.context = context;
        FlamebaseDatabase.urlServer = urlServer;
        FlamebaseDatabase.urlRedis = redisServer;
        FlamebaseDatabase.statusListener = statusListener;
        FlamebaseDatabase.debug = false;
        FlamebaseDatabase.gson = new Gson();
        SharedPreferences shared = context.getSharedPreferences("flamebase_config", MODE_PRIVATE);
        FlamebaseDatabase.id = shared.getString("flamebase_id", null);
        if (FlamebaseDatabase.id == null) {
            FlamebaseDatabase.id = generateNewId();
        }

        if (FlamebaseDatabase.pathMap == null) {
            FlamebaseDatabase.pathMap = new HashMap<>();
        }

        initialized = true;

        SC.init(context);
        ReferenceUtils.initialize(context);
        start();
    }

    private static String generateNewId() {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);;
        SharedPreferences.Editor shared = context.getSharedPreferences("flamebase_config", MODE_PRIVATE).edit();
        shared.putString("flamebase_id", id);
        shared.apply();
        return id;
    }

    /**
     * debug logs
     * @param debug
     */
    public static void setDebug(boolean debug) {
        FlamebaseDatabase.debug = debug;
    }

    /**
     * creates a listener for given path
     *
     * @param path
     * @param blower
     * @param clazz
     * @param <T>
     */
    public static <T> void createListener(final String path, Blower<T> blower, Class<T> clazz) {
        if (FlamebaseDatabase.pathMap == null) {
            Log.e(TAG, "Use FlamebaseDatabase.initialize(Context context, String urlServer, String token, StatusListener) before create real time references");
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("je", "je");

        if (flamebaseService == null || flamebaseService.getMoment() == null) {
            statusListener.reconnecting();
            return;
        }

        long blowerCreation = new Date().getTime();

        if (FlamebaseDatabase.pathMap.containsKey(path) && flamebaseService.getMoment().equals(FlamebaseDatabase.pathMap.get(path).getMoment())) {

            if (FlamebaseDatabase.debug) {
                Log.d(TAG, "Listener already added for: " + path);
            }
            pathMap.get(path).addBlower(blowerCreation, blower);
            pathMap.get(path).loadCachedReference();
            return;
        }

        Type type;
        if (blower instanceof MapBlower) {
            type = Type.MAP;
        } else {
            type = Type.OBJECT;
        }

        switch (type) {

            case MAP:

                final MapBlower<T> mapBlower = (MapBlower<T>) blower;

                final MapReference mapReference = new MapReference<T>(context, path, blowerCreation, mapBlower, clazz, flamebaseService.getMoment()) {

                    @Override
                    public void progress(int value) {
                        mapBlower.progress(value);
                    }

                };

                pathMap.put(path, mapReference);

                mapReference.loadCachedReference();

                syncWithServer(path);

                break;

            case OBJECT:

                final ObjectBlower<T> objectBlower = (ObjectBlower<T>) blower;

                final ObjectReference objectReference = new ObjectReference<T>(context, path, blowerCreation, objectBlower, clazz, flamebaseService.getMoment()) {

                    @Override
                    public void progress(int value) {
                        objectBlower.progress(value);
                    }

                };

                pathMap.put(path, objectReference);

                objectReference.loadCachedReference();

                syncWithServer(path);

                break;
        }

    }

    private static void syncWithServer(String path) {
        String content = ReferenceUtils.getElement(path);
        if (content == null) {
            content = EMPTY_OBJECT;
        }

        String sha1 = ReferenceUtils.SHA1(content);

        CreateListener createListener = new CreateListener("create_listener", path, FlamebaseDatabase.id, OS, sha1, content.length());

        Call<SyncResponse> call = ReferenceUtils.service(FlamebaseDatabase.urlServer).createReference(createListener);
        call.enqueue(new Callback<SyncResponse>() {

            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                if (response.errorBody() != null && !response.isSuccessful()) {
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                if (t.getStackTrace() != null) {
                    Log.e(TAG, "error");
                    t.printStackTrace();
                } else {
                    Log.e(TAG, "create listener response error");
                }
            }
        });
    }

    public static void removeListener(final String path) {
        if (pathMap.containsKey(path)) {
            RemoveListener removeListener = new RemoveListener("remove_listener", path, FlamebaseDatabase.id);
            Call<SyncResponse> call = ReferenceUtils.service(FlamebaseDatabase.urlServer).removeListener(removeListener);
            call.enqueue(new Callback<SyncResponse>() {

                @Override
                public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                    if (response.errorBody() != null && !response.isSuccessful()) {
                        try {
                            Log.e(TAG, response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SyncResponse> call, Throwable t) {
                    if (t.getStackTrace() != null) {
                        Log.e(TAG, "error");
                        t.printStackTrace();
                    } else {
                        Log.e(TAG, "remove listener response error");
                    }
                }
            });
        }
    }

    private static void refreshToServer(final String path, @NonNull String differences, @NonNull Integer len, boolean clean) {
        String content = ReferenceUtils.getElement(path);

        if (differences.equals(EMPTY_OBJECT)) {
            Log.e(FlamebaseDatabase.class.getSimpleName(), "no differences: " + differences);
            return;
        } else {
            Log.d(FlamebaseDatabase.class.getSimpleName(), "differences: " + differences);
        }

        if (content == null) {
            content = EMPTY_OBJECT;
        }
        String sha1 = ReferenceUtils.SHA1(content);

        UpdateToServer updateToServer = new UpdateToServer("update_data", path, FlamebaseDatabase.id, "android", differences, len, clean);
        Call<SyncResponse> call = ReferenceUtils.service(FlamebaseDatabase.urlServer).refreshToServer(updateToServer);

        call.enqueue(new Callback<SyncResponse>() {

            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                if (response.errorBody() != null && !response.isSuccessful()) {
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                if (t.getStackTrace() != null) {
                    Log.e(TAG, "error");
                    t.printStackTrace();
                } else {
                    Log.e(TAG, "update to server response error");
                }
            }
        });
    }

    public static void onMessageReceived(JSONObject jsonObject) {
        try {
            if (jsonObject.has("data")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data.has("info") && data.has(PATH)) {
                    String info = data.getString("info");
                    final String path = data.getString(PATH);
                    if (ACTION_NEW_OBJECT.equals(info)) {
                        if (pathMap.containsKey(path)) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sync(path);
                                }
                            }, 200);
                        }
                    }
                } else if (data.has(PATH)) {
                    String path = data.getString(PATH);
                    if (pathMap.containsKey(path)) {
                        pathMap.get(path).onMessageReceived(data);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sync(String path) {
        sync(path, false);
    }

    /**
     * Updates {@code Map<String, Reference> pathMap} invoking {@code syncReference()} on Reference object.
     *
     * @param clean
     */
    public static <T> void sync(String path, boolean clean) {
        if (pathMap.containsKey(path)) {
            Object[] result = pathMap.get(path).syncReference(clean);
            String diff = (String) result[1];
            Integer len = (Integer) result[0];
            if (!EMPTY_OBJECT.equals(diff)) {
                refreshToServer(path, diff, len, clean);
            } else {
                Blower<T> blower = (Blower<T>) pathMap.get(path).getLastest();
                String value = pathMap.get(path).getStringReference();
                if (value == null || value.equals(EMPTY_OBJECT) || value.equals(NULL)) {
                    blower.creatingObject();
                }
            }
        }
    }

    public static void syncReference(String path) {
        syncReference(path, false);
    }

    public static void syncReference(String path, boolean clean) {
        if (pathMap.containsKey(path)) {
            Object[] result = pathMap.get(path).syncReference(clean);
            String diff = (String) result[1];
            Integer len = (Integer) result[0];
            refreshToServer(path, diff, len, clean);
        }
    }

    public static void refreshFromServer(String path) {

        String content = ReferenceUtils.getElement(path);
        if (content == null) {
            content = EMPTY_OBJECT;
        }
        String sha1 = ReferenceUtils.SHA1(content);

        UpdateFromServer updateFromServer = new UpdateFromServer("get_updates", path, content, content.length(), FlamebaseDatabase.id, "android");
        Call<SyncResponse> call = ReferenceUtils.service(FlamebaseDatabase.urlServer).refreshFromServer(updateFromServer);

        call.enqueue(new Callback<SyncResponse>() {

            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                if (response.errorBody() != null && !response.isSuccessful()) {
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                if (t.getStackTrace() != null) {
                    t.printStackTrace();
                } else {
                    Log.e(TAG, "update from server response error");
                }
            }
        });
    }

    public static void stop() {
        if (isServiceBound != null && isServiceBound && flamebaseService != null && flamebaseService.getServiceConnection() != null) {
            flamebaseService.stopService();
            try {
                context.unbindService(flamebaseService.getServiceConnection());
            } catch (IllegalArgumentException e) {
                // nothing to do here
            }

            if (debug) Log.e(TAG, "unbound");
            context.stopService(new Intent(context, FlamebaseService.class));
            isServiceBound = false;
        }
    }

    private static void start() {
        if (isServiceBound == null || !isServiceBound) {
            Intent i = new Intent(context, FlamebaseService.class);
            context.startService(i);
            context.bindService(i, getServiceConnection(new FlamebaseService()), Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }
    }

    public static void onResume() {
        start();
    }

    public static void onPause() {
        if (flamebaseService != null && isServiceBound != null && isServiceBound) {
            context.unbindService(flamebaseService.getServiceConnection());
            isServiceBound = false;
        }
    }



    private static ServiceConnection getServiceConnection(Object obj) {
        if (obj instanceof FlamebaseService) {
            return serviceConnection;
        } else {
            return null;
        }
    }
}
