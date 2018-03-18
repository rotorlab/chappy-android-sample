package com.rotor.chappy.services;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by efraespada on 18/06/2017.
 */

public class LocalData {

    private static Context context;

    private LocalData() {
        // nothing to do here ..
   }

    public static void init(Context context) {
        LocalData.context = context;
    }

    public static JSONArray getLocalPaths() {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String localPath = prefs.getString("get_local_path", null);

        if (localPath == null) {
            return new JSONArray();
        } else {
            try {
                return new JSONArray(localPath);
            } catch (JSONException e) {
                e.printStackTrace();
                return new JSONArray();

            }
        }
    }

    public static void addPath(String value) {
        JSONArray array = getLocalPaths();
        boolean added = false;
        for (int i = 0; i < array.length(); i++) {
            try {
                String check = array.getString(i);
                if (check.equals(value)) {
                    added = true;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!added) {
            array.put(value);
        }
        setLocalPaths(array);
    }

    public static void removePath(String value) {
        JSONArray array = getLocalPaths();
        for (int i = 0; i < array.length(); i++) {
            try {
                String check = array.getString(i);
                if (check.equals(value)) {
                    array.remove(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setLocalPaths(array);
    }

    public static void setLocalPaths(JSONArray array) {
        SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
        editor.putString("get_local_path", array.toString()).apply();
    }
}
