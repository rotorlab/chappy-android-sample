package com.efraespada.jsondiff;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private static final String EMPTY_OBJECT = "{}";

    private static boolean debug = false;

    private JSONDiff() {
        // nothing to do here
    }

    public static Map<String, JSONObject> diff(JSONObject a, JSONObject b) {
        Map<String, Object> mapA = getMap(a);
        Map<String, Object> mapB = getMap(b);

        final Map<String, JSONObject> holder = new HashMap<>();
        holder.put(TAG_SET, new JSONObject());
        holder.put(TAG_UNSET, new JSONObject());
        holder.put(TAG_RENAME, new JSONObject());

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
     *
     * @param a
     * @return
     */
    private static Map<String, Object> getMap(JSONObject a) {
        Map<String, Object> map = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(a.toString(), new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return map;
        }
    }

    public static void setDebug(boolean debug) {
        JSONDiff.debug = debug;
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
                } else if ((valueA instanceof Long && valueB instanceof Long) || (valueA instanceof Integer && valueB instanceof Integer) || (valueA instanceof Float && valueB instanceof Float) || (valueA instanceof Double && valueB instanceof Double)) {
                    if (!valueA.equals(valueB)) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (valueA instanceof Map && valueB instanceof Map) {
                    if (((Map) valueB).isEmpty()) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, new JSONObject(EMPTY_OBJECT));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        hashMapper(holder, path + (path.length() == 0 ? "" : SEPARATOR) + keyA, (Map<Object, Object>) valueA, (Map<Object, Object>) valueB);
                    }
                } else if (valueA instanceof List && valueB instanceof List) {
                    try {
                        if (debug) {
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
                } else if (valueA instanceof Boolean && valueB instanceof Boolean) {
                    if (valueA != valueB) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (debug) {
                        Log.e(TAG, "not mapped value: " + String.valueOf(valueA) + " - " + valueA.getClass());
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
                } else if (valueB instanceof Boolean) {
                    try {
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Long || valueB instanceof Integer || valueB instanceof Float || valueB instanceof Double) {
                    try {
                        holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Map) {
                    if (((Map) valueB).isEmpty()) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : SEPARATOR) + keyB, new JSONObject(EMPTY_OBJECT));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        hashMapper(holder, path + (path.length() == 0 ? "" : SEPARATOR) + keyB, new HashMap<>(), (Map<Object, Object>) valueB);
                    }
                } else if (valueB instanceof List) {
                    try {
                        if (debug) {
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
                    if (debug) {
                        Log.e(TAG, "not mapped value: " + String.valueOf(valueB) + " - " + valueB.getClass());
                    }
                }
            }
        }
    }

    public static String hash(JSONObject a) {
        return hash(a.toString());
    }

    public static String hash(String a) {
        char[] val = a.toCharArray();
        long hashValue = 0;
        for (int i = 0; i < val.length; i++) {
            int codeValue = Character.codePointAt(val, i);
            if (codeValue > 0) {
                hashValue += codeValue;
            }
        }
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(String.valueOf(hashValue).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aResult : result) {
                sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
