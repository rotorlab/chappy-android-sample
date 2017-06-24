package com.flamebase.jsondiff;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 15/06/2017.
 */

public class JSONDiff {

    private static String TAG = JSONDiff.class.getSimpleName();

    private static final String SEPARATOR = ".";
    private static final String TAG_SET = "$set";
    private static final String TAG_UNSET = "$unset";
    private static final String TAG_RENAME = "$rename";

    private static boolean DEBUG = false;

    private JSONDiff() {
        // nothing to do here
    }

    public static Map<String, JSONObject> diff(JSONObject a, JSONObject b) {
        Map<String, Object> mapA = getMap(a);
        Map<String, Object> mapB = getMap(b);

        final Map<String, JSONObject> holder = new HashMap<>();
        holder.put("$set", new JSONObject());
        holder.put("$unset", new JSONObject());
        holder.put("$rename", new JSONObject());

        hashMapper(holder, "", mapA, mapB);

        if (emptySet(holder.get(TAG_SET))) {
            holder.remove(TAG_SET);
        }

        if (emptySet(holder.get(TAG_UNSET))) {
            holder.remove(TAG_UNSET);
        }

        if (emptySet(holder.get(TAG_RENAME))) {
            holder.remove(TAG_RENAME);
        }

        return holder;
    }

    private static boolean emptySet(JSONObject object) {
        return object.toString().length() <= 2;
    }

    /**
     * returns a map of the given JSON object
     * @param a
     * @return
     */
    private static Map<String, Object> getMap(JSONObject a) {
        Map<String, Object> map = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(a.toString(), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return map;
        }
    }

    public static void setDebug(boolean DEBUG) {
        JSONDiff.DEBUG = DEBUG;
    }

    public static <T, K, G> void hashMapper(final Map<String, JSONObject> holder, String path, Map<T, K> mapA, Map<T, K> mapB) {

        List<T> keysA = new ArrayList<>();
        for (Map.Entry<T, K> entryA : mapA.entrySet()) {
            keysA.add(entryA.getKey());
        }

        List<T> keysB = new ArrayList<>();
        for (Map.Entry<T, K> entryB : mapB.entrySet()) {
            keysB.add(entryB.getKey());
        }

        for (int a = 0; a < keysA.size(); a++) {
            T keyA = keysA.get(a);
            K valueA = mapA.get(keysA.get(a));

            if (mapB.containsKey(keyA)) {
                // TODO check differences

                T keyB = keyA;
                K valueB = mapB.get(keyB);

                if (valueA instanceof String && valueB instanceof String) {
                    if (!valueA.equals(valueB)) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ((valueA instanceof Long && valueB instanceof Long) || (valueA instanceof Integer && valueB instanceof Integer)) {
                    if (valueA != valueB) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (valueA instanceof Map && valueB instanceof Map) {
                    hashMapper(holder, path + (path.length() == 0 ? "" : SEPARATOR) + keyA, (Map<Object, Object>) valueA, (Map<Object, Object>) valueB);
                } else if (valueA instanceof List && valueB instanceof List) {
                    try {
                        if (DEBUG) {
                            Log.e(TAG, String.valueOf(valueB));
                        }
                        JSONArray arraySet = new JSONArray();
                        for (G elemB : (List<G>) valueB) {
                            if (!((List) valueA).contains(elemB)) {
                                arraySet.put(elemB);
                            }
                        }
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, arraySet);

                        JSONArray arrayUnset = new JSONArray();
                        for (G elemA : (List<G>) valueA) {
                            if (!((List) valueB).contains(elemA)) {
                                arrayUnset.put(elemA);
                            }
                        }

                        holder.get("$unset").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, arrayUnset);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "not mapped value: " + String.valueOf(valueA));
                    }
                }
            } else {
                try {
                    holder.get("$unset").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyA, valueA);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        for (int b = 0; b < keysB.size(); b++) {
            T keyB = keysB.get(b);
            K valueB = mapB.get(keysB.get(b));

            if (!mapA.containsKey(keyB)) {
                if (valueB instanceof String) {
                    try {
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Long || valueB instanceof Integer) {
                    try {
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Map) {
                    try {

                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, new JSONObject("{}"));
                        hashMapper(holder, path + (path.length() == 0 ? "" : SEPARATOR) + keyB, new HashMap<>(), (Map<Object, Object>) valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof List) {
                    try {
                        if (DEBUG) {
                            Log.e(TAG, String.valueOf(valueB));
                        }
                        JSONArray array = new JSONArray();
                        for (G elem : (List<G>) valueB) {
                            array.put(elem);
                        }
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, array);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "not mapped value: " + String.valueOf(valueB));
                    }
                }
            }
        }
    }
}
