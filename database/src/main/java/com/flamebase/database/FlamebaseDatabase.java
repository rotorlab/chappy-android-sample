package com.flamebase.database;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.flamebase.database.interfaces.Blower;
import com.flamebase.database.interfaces.FlamebaseResponse;
import com.flamebase.database.interfaces.ListBlower;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;
import com.flamebase.database.model.CallbackIO;
import com.flamebase.database.model.MapReference;
import com.flamebase.database.model.ObjectReference;
import com.flamebase.database.model.Reference;
import com.flamebase.database.model.TokenListener;
import com.flamebase.database.model.request.CreateListener;
import com.flamebase.database.model.request.RemoveListener;
import com.flamebase.database.model.request.UpdateFromServer;
import com.flamebase.database.model.request.UpdateToServer;
import com.flamebase.database.model.service.SyncResponse;
import com.google.gson.Gson;
import com.stringcare.library.SC;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.socket.client.Ack;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.flamebase.database.model.Reference.EMPTY_OBJECT;
import static com.flamebase.database.model.Reference.PATH;

/**
 * Created by efraespada on 10/06/2017.
 */

public class FlamebaseDatabase {

    private static final String TAG = FlamebaseDatabase.class.getSimpleName();

    private static final String KEY = "database";
    private static final String OS = "android";
    private static Context context;
    private static TokenListener listener;
    public static String id;
    private static String urlServer;
    public static String urlRedis;
    private static String token;

    private static FlamebaseService flamebaseService;
    private static Boolean isServiceBound;

    private Long blowerCreation;
    private String path;
    private int requestToClose;

    private static Gson gson;
    public static Boolean debug = false;

    private static HashMap<String, Reference> pathMap;

    public enum Type {
        OBJECT,
        LIST,
        MAP
    }

    private FlamebaseDatabase() {
        requestToClose = 0;
    }

    private Socket getSocketIO() {
        return SocketIO.getInstance(urlServer, KEY, jsonObject -> {
            Log.d(FlamebaseDatabase.class.getSimpleName(), jsonObject.toString());
            onMessageReceived(jsonObject);
        });
    }

    /**
     * Set initial config to createReference with flamebase server cluster
     *
     * @param context
     * @param urlServer
     */
    public static void initialize(Context context, String urlServer, String redisServer) {
        FlamebaseDatabase.context = context;
        FlamebaseDatabase.urlServer = urlServer;
        FlamebaseDatabase.urlRedis = redisServer;
        FlamebaseDatabase.gson = new Gson();
        FlamebaseDatabase.token = getToken();
        SharedPreferences shared = context.getSharedPreferences("flamebase_config", MODE_PRIVATE);
        FlamebaseDatabase.id = shared.getString("flamebase_id", null);
        if (FlamebaseDatabase.id == null) {
            FlamebaseDatabase.id = generateNewId();
        }

        if (FlamebaseDatabase.pathMap == null) {
            FlamebaseDatabase.pathMap = new HashMap<>();
        }

        SC.init(context);
        ReferenceUtils.initialize(context);
        start();
    }

    private static String generateNewId() {
        String id = UUID.randomUUID().toString();
        SharedPreferences.Editor shared = context.getSharedPreferences("flamebase_config", MODE_PRIVATE).edit();
        shared.putString("flamebase_id", id);
        shared.apply();
        return id;
    }

    private static void print(Object... args) {
        JSONObject o = (JSONObject) args[0];
        Log.e(FlamebaseDatabase.class.getSimpleName(), "lerelele: " + o.toString());
    }

    /**
     * debug logs
     * @param debug
     */
    public static void setDebug(boolean debug) {
        FlamebaseDatabase.debug = debug;
    }

    public static FlamebaseDatabase getInstance() {
        return new FlamebaseDatabase();
    }

    public static String getToken() {
        return ReferenceUtils.SHA1(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
    }

    /**
     * creates a listener for given path
     *
     * @param path
     * @param blower
     * @param clazz
     * @param <T>
     */
    public <T> FlamebaseDatabase createListener(final String path, Blower<T> blower, Class<T> clazz) {
        if (FlamebaseDatabase.pathMap == null) {
            Log.e(TAG, "Use FlamebaseDatabase.initialize(Context context, String urlServer, String token) before create real time references");
            return null;
        }

        this.path = path;

        blowerCreation = new Date().getTime();

        if (FlamebaseDatabase.pathMap.containsKey(this.path)) {
            if (FlamebaseDatabase.debug) {
                Log.d(TAG, "Listener already added for: " + this.path);
            }
            pathMap.get(this.path).addBlower(blowerCreation, blower);
            pathMap.get(this.path).loadCachedReference();
            return this;
        }

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

                final MapReference mapReference = new MapReference<T>(context, this.path, blowerCreation, mapBlower, clazz) {

                    @Override
                    public void progress(int value) {
                        mapBlower.progress(value);
                    }

                };

                pathMap.put(this.path, mapReference);

                //mapReference.loadCachedReference();

                syncWithServer(this.path);

                break;

            case OBJECT:

                final ObjectBlower<T> objectBlower = (ObjectBlower<T>) blower;

                final ObjectReference objectReference = new ObjectReference<T>(context, this.path, blowerCreation, objectBlower, clazz) {

                    @Override
                    public void progress(int value) {
                        objectBlower.progress(value);
                    }

                };

                pathMap.put(this.path, objectReference);

                //objectReference.loadCachedReference();

                syncWithServer(this.path);

                break;
        }

        //syncReference(this.path, true);

        return this;
    }

    private void syncWithServer(String path) {
        String content = ReferenceUtils.getElement(this.path);
        if (content == null) {
            content = EMPTY_OBJECT;
        }
        String sha1 = ReferenceUtils.SHA1(content);

        CreateListener createListener = new CreateListener("create_listener", this.path, token, OS, sha1, content.length());

        Gson gson = new Gson();
        getSocketIO().emit(KEY, gson.toJson(createListener, CreateListener.class), new Ack() {
            @Override
            public void call(Object... args) {
                print((JSONObject) args[0]);
            }
        });
    }

    public void removeListener() {
        removeListener(this.path);
    }

    public void removeListener(final String path) {
        if (pathMap.containsKey(path)) {
            Reference reference = pathMap.get(path);
            if (reference.blowerMap.size() <= 1 || (reference.blowerMap.size() > 1 && reference.blowerMap.size() == requestToClose)) {
                RemoveListener removeListener = new RemoveListener("remove_listener", path, token);
                Gson gson = new Gson();
                getSocketIO().emit(KEY, gson.toJson(removeListener, RemoveListener.class), new Ack() {
                    @Override
                    public void call(Object... args) {
                        print((JSONObject) args[0]);
                    }
                });
            } else {
                requestToClose++;
            }
        }
    }

    private void refreshToServer(final String path, @NonNull String differences, @NonNull Integer len, boolean clean) {
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

        UpdateToServer updateToServer = new UpdateToServer("update_data", path, FlamebaseDatabase.token, "android", differences, len, clean);

        Gson gson = new Gson();
        getSocketIO().emit(KEY, gson.toJson(updateToServer, UpdateToServer.class), new Ack() {
            @Override
            public void call(Object... args) {
                print((JSONObject) args[0]);
            }
        });
    }

    public static void onMessageReceived(JSONObject jsonObject) {
        try {
            if (jsonObject.has("data")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data.has(PATH)) {
                    String path = data.getString(PATH);
                    if (pathMap.containsKey(path)) {
                        //Log.d(FlamebaseDatabase.class.getSimpleName(), data.toString());
                        pathMap.get(path).onMessageReceived(data);
                    } else {

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String path = remoteMessage.getData().get(PATH);
            if (pathMap.containsKey(path)) {
                Log.d(FlamebaseDatabase.class.getSimpleName(), remoteMessage.getData().toString());
                pathMap.get(path).onMessageReceived(remoteMessage);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    */

    public void sync() {
        sync(false);
    }

    /**
     * Updates {@code Map<String, Reference> pathMap} invoking {@code syncReference()} on Reference object.
     *
     * @param clean
     */
    public void sync(boolean clean) {
        if (pathMap.containsKey(this.path)) {
            Object[] result = pathMap.get(this.path).syncReference(clean);
            String diff = (String) result[1];
            Integer len = (Integer) result[0];
            refreshToServer(this.path, diff, len, clean);
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
            new FlamebaseDatabase().refreshToServer(path, diff, len, clean);
        }
    }

    public static void refreshFromServer(String path, final FlamebaseResponse callback) {

        String content = ReferenceUtils.getElement(path);
        if (content == null) {
            content = EMPTY_OBJECT;
        }
        String sha1 = ReferenceUtils.SHA1(content);

        UpdateFromServer updateFromServer = new UpdateFromServer("get_updates", path, content, content.length(), token, "android");
        Call<SyncResponse> call = ReferenceUtils.service(FlamebaseDatabase.urlServer).refreshFromServer(updateFromServer);

        call.enqueue(new Callback<SyncResponse>() {

            @Override
            public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                SyncResponse syncResponse = response.body();
                if (!syncResponse.getData().toString().equals(EMPTY_OBJECT)) {
                    callback.onSuccess(syncResponse.getData());
                } else {
                    callback.onFailure(syncResponse.getError());
                }
            }

            @Override
            public void onFailure(Call<SyncResponse> call, Throwable t) {
                if (t.getStackTrace() != null) {
                    callback.onFailure(t.getStackTrace().toString());
                } else {
                    callback.onFailure("refresh from server response error");
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
        /*
          else {
            flamebaseService.stopService();
            flamebaseService.startService();
        }
        */
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
        if (obj instanceof FlamebaseService) return new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                if (service instanceof FlamebaseService.FBinder) {
                    flamebaseService = ((FlamebaseService.FBinder) service).getService();
                    flamebaseService.setServiceConnection(this);
                    if (debug) Log.e(TAG, "instanced service");
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                if (className.getClassName().equals(FlamebaseService.class.getName())) flamebaseService = null;
                if (debug) Log.e(TAG, "disconnected");
            }
        };
        return null;
    }
}
