package com.flamebase.database.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.efraespada.androidstringobfuscator.AndroidStringObfuscator;
import com.flamebase.database.Database;
import com.flamebase.database.ReferenceUtils;
import com.flamebase.jsondiff.JSONDiff;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.flamebase.database.Database.COLUMN_DATA;
import static com.flamebase.database.Database.COLUMN_ID;

/**
 * Created by efraespada on 21/05/2017.
 */

public abstract class Reference {

    private int VERSION = 1;
    private static Map<String, String[]> mapParts;
    private Database database;
    private Context context;

    public int len;

    private static final String TAG = Reference.class.getSimpleName();

    public static String STAG = "tag";
    public static String PATH = "id";
    public static String REFERENCE = "reference";
    public static String TABLE_NAME = "ref";
    public static String SIZE = "size";
    public static String INDEX = "index";
    public static String ACTION = "action";

    public String path;
    public String stringReference;

    public static final String ACTION_SIMPLE_UPDATE    = "simple_update";
    public static final String ACTION_SLICE_UPDATE     = "slice_update";

    public Reference(Context context, String path) {
        this.context = context;
        this.path = path;
        AndroidStringObfuscator.init(this.context);
        // String name = Reference.class.getSimpleName() + ".db";
        String name = "RealtimeDatabase.db";
        this.database = new Database(this.context, name, TABLE_NAME, VERSION);
        this.mapParts = new HashMap<>();
        this.len = 0;
        this.stringReference = getElement(path);
    }

    public Reference(Context context, String path, RemoteMessage remoteMessage) {
        this.context = context;
        this.path = path;
        AndroidStringObfuscator.init(this.context);
        String name = "RealtimeDatabase.db";
        this.database = new Database(this.context, name, TABLE_NAME, VERSION);
        this.len = 0;
        this.stringReference = "{}";
        onMessageReceived(remoteMessage);
    }

    /**
     * checks if push message comes from server cluster
     * @param remoteMessage
     */
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String tag = remoteMessage.getData().get(STAG);
            String action = remoteMessage.getData().get(ACTION);
            String data = remoteMessage.getData().get(REFERENCE);
            String path = remoteMessage.getData().get(PATH);
            String rData = ReferenceUtils.hex2String(data);

            if (!tag.equalsIgnoreCase(getTag())) {
                return;
            }

            switch (action) {

                case ACTION_SIMPLE_UPDATE:
                    parseResult(path, rData);
                    break;

                case ACTION_SLICE_UPDATE:
                    int size = Integer.parseInt(remoteMessage.getData().get(SIZE));
                    int index = Integer.parseInt(remoteMessage.getData().get(INDEX));
                    if (mapParts.containsKey(path)) {
                        mapParts.get(path)[index] = rData;
                    } else {
                        String[] parts = new String[size];
                        parts[index] = rData;
                        mapParts.put(path, parts);
                    }

                    boolean ready = true;
                    int alocated = 0;
                    for (int p = mapParts.get(path).length - 1; p >= 0; p--) {
                        if (mapParts.get(path)[p] == null) {
                            ready = false;
                        } else {
                            alocated++;
                        }
                    }

                    float percent = (100F / (float) size) * alocated;
                    progress((int) percent);

                    if (ready && mapParts.get(path).length - 1 == index) {
                        StringBuilder complete = new StringBuilder();
                        for (int i = 0; i < mapParts.get(path).length; i++) {
                            complete.append(mapParts.get(path)[i]);
                        }
                        mapParts.remove(path);
                        String result = complete.toString();
                        parseResult(path, result);
                    }

                    break;

                default:
                    // nothing to do here ..
                    break;

            }

            //Log.e(TAG, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * notify update percent
     * @param value
     */
    public abstract void progress(int value);

    /**
     * tag or identifier used to identify incoming object updates
     * from server cluster
     *
     * @return
     */
    public abstract String getTag();

    /**
     * returns actual reference in string format
     * @return String
     */
    public abstract String getStringReference();

    /**
     * loads stored JSON object on db. if not exists,
     * gets current reference and stores
     *
     */
    public abstract void loadCachedReference();

    /**
     * returns the result of applying differences to current JSON object
     * after being stored on local DB
     * @param value
     */
    public abstract void blowerResult(String value);

    /**
     * updates current string object with incoming data
     * @param path
     * @param data
     */
    private void parseResult(String path, String data) {
        try {
            JSONObject jsonObject;
            String prev = getStringReference();
            if (prev != null) {
                prev = Normalizer.normalize(prev, Normalizer.Form.NFC);
                jsonObject = new JSONObject(prev);
            } else {
                jsonObject = new JSONObject();
            }

            JSONObject differences = new JSONObject(data);

            if (differences.has("$unset")) {
                JSONObject set = differences.getJSONObject("$unset");
                Iterator<String> keys = set.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String[] p = key.split("\\.");
                    JSONObject aux = jsonObject;

                    for (int w = 0; w < p.length; w++) {
                        String currentIndex = p[w];
                        if (aux.has(currentIndex) && w != p.length - 1) {
                            aux = aux.getJSONObject(currentIndex);
                        } else if (w != p.length - 1) {
                            aux.put(currentIndex, new JSONObject());
                            aux = aux.getJSONObject(currentIndex);
                        }

                        if (w == p.length - 1 && aux.has(currentIndex)) {
                            aux.remove(currentIndex);
                        }
                    }
                }
            }

            if (differences.has("$set")) {
                JSONObject set = differences.getJSONObject("$set");
                Iterator<String> keys = set.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String[] p = key.split("\\.");
                    JSONObject aux = jsonObject;

                    for (int w = 0; w < p.length; w++) {
                        String currentIndex = p[w];
                        if (aux.has(currentIndex) && w != p.length - 1) {
                            aux = aux.getJSONObject(currentIndex);
                        } else if (w != p.length - 1) {
                            aux.put(currentIndex, new JSONObject());
                            aux = aux.getJSONObject(currentIndex);
                        }

                        if (w == p.length - 1) {
                            if (aux.has(currentIndex)) {
                                try {
                                    aux = aux.getJSONObject(currentIndex);
                                    JSONObject toExport = set.getJSONObject(key);
                                    Iterator<String> y = toExport.keys();
                                    while (y.hasNext()) {
                                        String k = y.next();
                                        aux.put(k, toExport.get(k));
                                    }
                                } catch (Exception e) {
                                    aux.put(currentIndex, set.get(key));
                                }
                            } else {
                                try {
                                    // test if element to save is JSON object
                                    String cached = set.getJSONObject(key).toString();
                                    cached = Normalizer.normalize(cached, Normalizer.Form.NFC);
                                    JSONObject object = new JSONObject(cached);
                                    aux.put(currentIndex, object);
                                } catch (Exception e) {
                                    aux.put(currentIndex, set.get(key));
                                }
                            }
                        }
                    }
                }
            }

            addElement(path, jsonObject.toString());
            stringReference = jsonObject.toString();
            blowerResult(stringReference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * updates stored path
     * @param path
     * @param info
     */
    public void addElement(String path, String info) {
        this.len = info.length();
        try {
            String enId = AndroidStringObfuscator.encryptString(path);
            // Gets the data repository in write mode
            SQLiteDatabase db = database.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, enId);
            values.put(COLUMN_DATA, AndroidStringObfuscator.encryptString(info));

            if (getElement(path) != null) {
                // Filter results WHERE "title" = hash
                String selection = COLUMN_ID + " = ?";
                String[] selectionArgs = { enId };
                long newRowId = db.update(database.table, values, selection, selectionArgs);
            } else {
                long newRowId = db.insert(database.table, null, values);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    /**
     * returns stored object
     * @param path
     * @return String
     */
    public String getElement(String path) {
        String enPath = AndroidStringObfuscator.encryptString(path);
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    COLUMN_ID,
                    COLUMN_DATA
            };

            // Filter results WHERE "title" = hash
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = { enPath };

            Cursor cursor = db.query(
                    database.table,                             // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                      // The sort order
            );

            String info = null;
            while (cursor.moveToNext()) {
                info = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA));
            }
            cursor.close();

            String res = AndroidStringObfuscator.decryptString(info);
            if (res == null) {
                this.len = 0;
            } else {
                this.len = res.length();
            }
            return res;
        } catch (SQLiteException e) {
            this.len = 0;
            return null;
        }
    }

    public Object[] syncReference(boolean clean) {
        int len;
        Gson gson = new Gson();
        Object[] objects = new Object[2];

        if (clean) {
            this.stringReference = "{}";
        } else if (stringReference == null) {
            this.stringReference = "{}";
        }

        try {
            String actual = getStringReference();

            Map<String, JSONObject> diff = JSONDiff.diff(new JSONObject(stringReference), new JSONObject(actual));

            JSONObject jsonObject = new JSONObject();

            // max 3
            for (Map.Entry<String, JSONObject> entry : diff.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }

            len = actual.length();

            objects[0] = len;
            objects[1] = jsonObject.toString();

            return objects;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return objects;
    }
}
