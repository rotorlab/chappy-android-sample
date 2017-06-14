package com.flamebase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.efraespada.androidstringobfuscator.AndroidStringObfuscator;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.flamebase.database.Database.COLUMN_LOCATION_ID;
import static com.flamebase.database.Database.COLUMN_LOCATION_INFO;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

/**
 * Created by efraespada on 21/05/2017.
 */

public abstract class RealtimeDatabase<T> {

    private int VERSION = 1;
    private static Map<String, String[]> mapParts;
    private Database database;
    private Class<T> clazz;
    private Context context;

    private T reference;

    private static final String TAG = RealtimeDatabase.class.getSimpleName();

    public static String STAG = "tag";
    public static String PATH = "id";
    public static String REFERENCE = "reference";
    public static String SIZE = "size";
    public static String INDEX = "index";
    public static String ACTION = "action";

    public static final String ACTION_SIMPLE_UPDATE    = "simple_update";
    public static final String ACTION_SLICE_UPDATE     = "slice_update";

    public RealtimeDatabase(Context context, Class<T> clazz) {
        this.clazz = clazz;
        this.context = context;
        AndroidStringObfuscator.init(this.context);
        String name = RealtimeDatabase.class.getSimpleName() + "_" + clazz.getSimpleName() + ".db";
        this.database = new Database(this.context, name, clazz.getSimpleName(), VERSION);
        this.mapParts      = new HashMap<>();
        reference = null;
    }

    public RealtimeDatabase(Context context, Class<T> clazz, RemoteMessage remoteMessage) {
        this.clazz = clazz;
        this.context = context;
        AndroidStringObfuscator.init(this.context);
        String name = RealtimeDatabase.class.getSimpleName() + "_" + clazz.getSimpleName() + ".db";
        this.database = new Database(this.context, name, clazz.getSimpleName(), VERSION);
        reference = null;
        onMessageReceived(remoteMessage);
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String tag = remoteMessage.getData().get(STAG);
            String action = remoteMessage.getData().get(ACTION);
            String data = remoteMessage.getData().get(REFERENCE);
            String path = remoteMessage.getData().get(PATH);
            String rData = hex2String(data);

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
                    progress(path, (int) percent);

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

    private String hex2String(String arg) {
        String str = "";
        for (int i = 0; i < arg.length(); i += 2) {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }
        return str;
    }

    public abstract void onObjectChanges(T value);

    public abstract T update();

    public abstract void progress(String id, int value);

    public abstract String getTag();

    private void parseResult(String path, String data) {
        try {
            Gson gson = new Gson();

            JSONObject jsonObject;
            String prev = getElement(path);
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
            reference = gson.fromJson(jsonObject.toString(), clazz);
            onObjectChanges(reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addElement(String path, String info) {
        String enId = AndroidStringObfuscator.encryptString(path);
        // Gets the data repository in write mode
        SQLiteDatabase db = database.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOCATION_ID, enId);
        values.put(COLUMN_LOCATION_INFO, AndroidStringObfuscator.encryptString(info));

        try {
            if (getElement(path) != null) {
                // Filter results WHERE "title" = hash
                String selection = COLUMN_LOCATION_ID + " = ?";
                String[] selectionArgs = { enId };
                long newRowId = db.update(database.table, values, selection, selectionArgs);
            } else {
                long newRowId = db.insert(database.table, null, values);
            }
        } catch (Exception e) {

        }
    }

    private String getElement(String path) {
        String enPath = AndroidStringObfuscator.encryptString(path);
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    COLUMN_LOCATION_ID,
                    COLUMN_LOCATION_INFO
            };

            // Filter results WHERE "title" = hash
            String selection = COLUMN_LOCATION_ID + " = ?";
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
                info = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_INFO));
            }
            cursor.close();

            return AndroidStringObfuscator.decryptString(info);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public <T> String syncReference(String path, T reference) {
        String diffReference =  "";
        Gson gson = new Gson();
        if (this.reference == null) {
            try {
                String expected = new JSONObject("{}").toString();
                String actual = gson.toJson(reference, clazz);
                JSONCompareResult result = JSONCompare.compareJSON(expected, actual, JSONCompareMode.STRICT);
                if (result.toString().contains("Unexpected")) {
                    String res = result.toString().trim().replace("Unexpected: ", "");
                    JSONObject jsonObject = new JSONObject(actual);

                    JSONObject diff = new JSONObject();
                    JSONObject unexpected = new JSONObject();
                    String[] unexpectedParts = res.split(" ; ");

                    for (int i = 0; i < unexpectedParts.length; i++) {
                        JSONObject toCheck = (JSONObject) getUnexpected(unexpectedParts[i], jsonObject);
                        unexpected.put(res, toCheck);
                    }

                    diff.put("$set", unexpected);
                    Log.e(TAG, "differences: " + diff.toString());
                    diffReference = diff.toString();
                } else {
                    Log.e(TAG, "CHECK THISSSS");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // TODO calculate here JSON differences
            try {
                String expected = gson.toJson(this.reference, clazz);
                String actual = gson.toJson(reference, clazz);
                JSONCompareResult result = JSONCompare.compareJSON(expected, actual, JSONCompareMode.STRICT);
                if (result.toString().contains("Unexpected")) {
                    String res = result.toString().trim().replace("Unexpected: ", "");
                    JSONObject jsonObject = new JSONObject(actual);

                    JSONObject toCheck = (JSONObject) getUnexpected(res, jsonObject);
                    JSONObject diff = new JSONObject();
                    JSONObject unexpected = new JSONObject();
                    unexpected.put(res, toCheck);
                    diff.put("$set", unexpected);
                    Log.e(TAG, "differences: " + diff.toString());
                    diffReference = diff.toString();
                } else {
                    Log.e(TAG, "CHECK THISSSS");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return diffReference;
    }

    private Object getUnexpected(String toFind, Object check) {
        Object object = null;

        try {
            Iterator<String> iter = ((JSONObject)check).keys();
            while (iter.hasNext()) {

                String key = iter.next();
                if (key.equalsIgnoreCase(toFind)) {
                    try {
                        object = ((JSONObject)check).get(key);
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        object = null;
                    }
                } else {
                    try {
                        object = getUnexpected(toFind, ((JSONObject)check).get(key));
                    } catch (JSONException e) {
                        try {
                            JSONArray array = (JSONArray) getUnexpected(toFind, (JSONObject) ((JSONObject)check).get(key));
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                Object resu = getUnexpected(toFind, obj);
                                if (resu != null) {
                                    object = resu;
                                    break;
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            object = null;
                        }
                        //e.printStackTrace();
                    }
                }
            }
        } catch (ClassCastException e) {
            try {
                JSONArray jsonArray = (JSONArray) check;

                for (int si = jsonArray.length() - 1; si >= 0; si--) {
                    JSONObject js = jsonArray.getJSONObject(si);
                    Object resu = getUnexpected(toFind, js);
                    if (resu != null) {
                        object = resu;
                        break;
                    }
                }

            } catch (JSONException | ClassCastException l) {
                object = check;
            }
        }


        return object;
    }

}
