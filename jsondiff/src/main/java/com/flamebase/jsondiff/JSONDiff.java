package com.flamebase.jsondiff;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 15/06/2017.
 */

public class JSONDiff {

    private static String TAG = JSONDiff.class.getSimpleName();

    private JSONDiff() {
        // nothing to do here
    }

    public static JSONObject diff(JSONObject a, JSONObject b) {
        JSONObject jsonObject = new JSONObject();

        Map<String, Object> mapA = getMap(a);
        Map<String, Object> mapB = getMap(b);

        Log.e(TAG, "mapA size " + mapA.size());
        Log.e(TAG, "mapB size " + mapB.size());

        return jsonObject;
    }

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
}
