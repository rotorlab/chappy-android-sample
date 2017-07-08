package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.MapBlower;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class MapReference<T> extends Reference {

    public MapBlower<T> blower;
    public Class<T> clazz;

    public MapReference(Context context, String path, MapBlower<T> blower, Class<T> clazz) {
        super(context, path);
        this.blower = blower;
        this.clazz = clazz;
    }

    public MapReference(Context context, String path, MapBlower<T> blower, Class<T> clazz, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        this.blower = blower;
        this.clazz = clazz;
    }

    /**
     * called when map is synchronized with cluster or path
     * not exists in local db and is stored (cached references)
     * @return Map - String, T
     */
    public abstract Map<String, T> updateMap();

    @Override
    public void blowerResult(String value) {
        if (!isSynchronized && value.length() == serverLen) {
            isSynchronized = true;
        }

        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(value, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        blower.onMapChanged(map);
    }

    @Override
    public String getStringReference() {
        String val;
        if (updateMap() == null) {
            if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                val = stringReference;
            } else {
                val = EMPTY_OBJECT;
            }
        } else {
            val = gson.toJson(updateMap(), getType(clazz));
        }

        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(val, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        blower.onMapChanged(map);

        return val;
    }

    @Override
    public void loadCachedReference() {
        stringReference = ReferenceUtils.getElement(path);
        if (stringReference == null) {
            stringReference = getStringReference();
            ReferenceUtils.addElement(path, stringReference);
        }

        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(stringReference, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        blower.onMapChanged(map);
    }

/*
    public <V> void loadChachedReference(ListBlower<V> blower, Class<V> clazz) {
        Gson gson = new Gson();
        stringReference = getElement(path);

        if (stringReference != null) {
            List<V> list = gson.fromJson(stringReference, new ListOf<>(clazz));
            blower.onListChanged(list);
        }
    }
*/

    class ListOf<X> implements ParameterizedType {

        private Class<?> wrapped;

        public ListOf(Class<X> wrapped) {
            this.wrapped = wrapped;
        }

        public Type[] getActualTypeArguments() {
            return new Type[] {wrapped};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }

    }

    private static <T> Type getType(Class<T> type) {
        return new TypeToken<Map<String, T>>() {}
                .where(new TypeParameter<T>() {}, type)
                .getType();
    }
}
