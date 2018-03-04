package com.flamebase.database.model;

import android.content.Context;

import com.efraespada.jsondiff.JSONDiff;
import com.flamebase.database.Database;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.SC;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by efraespada on 21/05/2017.
 */

public abstract class Reference<T> {

    private int VERSION = 1;
    private static Map<String, String[]> mapParts;
    public Database database;
    private Context context;
    protected Gson gson;
    public Map<Long, T> blowerMap;

    public boolean isSynchronized;

    public int len;
    public int serverLen;
    public int queueLen;

    private static final String TAG = Reference.class.getSimpleName();

    public static String STAG = "tag";
    public static String PATH = "id";
    public static String REFERENCE = "reference";
    public static String TABLE_NAME = "ref";
    public static String SIZE = "size";
    public static String INDEX = "index";
    public static String ACTION = "action";
    public static String EMPTY_OBJECT = "{}";

    protected String path;
    protected String stringReference;
    protected Long moment;

    public static final String ACTION_SIMPLE_UPDATE     = "simple_update";
    public static final String ACTION_SLICE_UPDATE      = "slice_update";
    public static final String ACTION_NO_UPDATE         = "no_update";
    public static final String ACTION_SIMPLE_CONTENT    = "simple_content";
    public static final String ACTION_SLICE_CONTENT     = "slice_content";
    public static final String ACTION_NO_CONTENT        = "no_content";
    public static final String ACTION_NEW_OBJECT        = "new_object";

    public Reference(Context context, String path, Long moment) {
        this.context = context;
        this.path = path;
        this.gson = getGsonBuilder();
        this.serverLen = 0;
        SC.init(this.context);
        this.mapParts = new HashMap<>();
        this.stringReference = ReferenceUtils.getElement(path);
        this.len = stringReference == null ? 0 : stringReference.length();
    }

    /**
     * checks if push message comes from server cluster
     */
    public void onMessageReceived(JSONObject json) {
        try {
            String tag = json.getString(STAG);
            String action = json.getString(ACTION);
            String data = json.has(REFERENCE) ? json.getString(REFERENCE) : null;
            String path = json.getString(PATH);
            String rData = data == null ? "{}" : ReferenceUtils.hex2String(data);


            if (!tag.equalsIgnoreCase(getTag())) {
                return;
            }

            switch (action) {

                case ACTION_SIMPLE_UPDATE:
                    parseUpdateResult(path, rData);
                    break;

                case ACTION_SLICE_UPDATE:
                    int size = json.getInt(SIZE);
                    int index = json.getInt(INDEX);
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
                        parseUpdateResult(path, result);
                    }

                    break;

                case ACTION_NO_UPDATE:
                    blowerResult(stringReference);
                    break;

                case ACTION_SIMPLE_CONTENT:
                    parseContentResult(path, rData);
                    break;

                case ACTION_SLICE_CONTENT:
                    int sizeContent = json.getInt(SIZE);
                    int indexContent = json.getInt(INDEX);
                    if (mapParts.containsKey(path)) {
                        mapParts.get(path)[indexContent] = rData;
                    } else {
                        String[] partsContent = new String[sizeContent];
                        partsContent[indexContent] = rData;
                        mapParts.put(path, partsContent);
                    }

                    boolean readyContent = true;
                    int alocatedContent = 0;
                    for (int p = mapParts.get(path).length - 1; p >= 0; p--) {
                        if (mapParts.get(path)[p] == null) {
                            readyContent = false;
                        } else {
                            alocatedContent++;
                        }
                    }

                    float percentContent = (100F / (float) sizeContent) * alocatedContent;
                    progress((int) percentContent);

                    if (readyContent && mapParts.get(path).length - 1 == indexContent) {
                        StringBuilder completeContent = new StringBuilder();
                        for (int i = 0; i < mapParts.get(path).length; i++) {
                            completeContent.append(mapParts.get(path)[i]);
                        }
                        mapParts.remove(path);
                        String resultContent = completeContent.toString();
                        parseContentResult(path, resultContent);
                    }

                    break;

                case ACTION_NO_CONTENT:
                    blowerResult("{}");
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
     */
    public abstract void progress(int value);

    public abstract void addBlower(long creation, T blower);

    /**
     * tag or identifier used to identify incoming object updates
     * from server cluster
     *
     * @return String
     */
    public String getTag() {
        return path + "_sync";
    }

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
    private void parseUpdateResult(String path, String data) {
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
                                if (aux.get(currentIndex) instanceof JSONObject) {
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

            ReferenceUtils.addElement(path, jsonObject.toString());
            stringReference = jsonObject.toString();
            this.len = stringReference.length();
            blowerResult(stringReference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * updates current string object with incoming data
     * @param path
     * @param data
     */
    private void parseContentResult(String path, String data) {
        ReferenceUtils.addElement(path, data);
        stringReference = data;
        this.len = stringReference.length();
        blowerResult(stringReference);
    }

    /**
     * Returns a {@code Object[]} object. If {@code clean} param is TRUE
     * differences var is built from empty JSON object.
     * Index 0: differences value length
     * Index 1: differences value
     * @param clean
     * @return Object[]
     */
    public Object[] syncReference(boolean clean) {
        int len;
        Gson gson = new Gson();
        Object[] objects = new Object[2];

        if (clean || stringReference == null) {
            this.stringReference = "{}";
        }

        try {
            String actual = getStringReference();
            JSONDiff.setDebug(FlamebaseDatabase.debug);
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

    public Long getMoment() {
        return moment;
    }

    private Gson getGsonBuilder() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

}
