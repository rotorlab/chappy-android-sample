package com.flamebase.jsondiff;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 15/06/2017.
 */

public class JSONDiff {

    private static String TAG = JSONDiff.class.getSimpleName();

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

        return holder;
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

    public static <T, K> void hashMapper(final Map<String, JSONObject> holder, String path, Map<T, K> mapA, Map<T, K> mapB) {

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
                            holder.get("$set").put(path + (path.length() == 0 ? "" : ".") + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ((valueA instanceof Long && valueB instanceof Long) || (valueA instanceof Integer && valueB instanceof Integer)) {
                    if (valueA != valueB) {
                        try {
                            holder.get("$set").put(path + (path.length() == 0 ? "" : ".") + keyA, valueB);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (valueA instanceof Map && valueB instanceof Map) {
                    hashMapper(holder, path + (path.length() == 0 ? "" : ".") + keyA, (Map<Object, Object>) valueA, (Map<Object, Object>) valueB);
                } else {
                    Log.e(TAG, "not mapped value: " + String.valueOf(valueA));
                }
            } else {
                try {
                    holder.get("$unset").put(path + (path.length() == 0 ? "" : ".") + keyA, valueA);
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
                        holder.get("$set").put(path + (path.length() == 0 ? "" : ".") + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Long || valueB instanceof Integer) {
                    try {
                        holder.get("$set").put(path + (path.length() == 0 ? "" : ".") + keyB, valueB);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (valueB instanceof Map) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String result = objectMapper.writeValueAsString(valueB);

                        holder.get("$set").put(path + (path.length() == 0 ? "" : ".") + keyB, new JSONObject(result));

                    } catch (JsonProcessingException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "not mapped value: " + String.valueOf(valueB));
                }
            }
        }
    }
}
