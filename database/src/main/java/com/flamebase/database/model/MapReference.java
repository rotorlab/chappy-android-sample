package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.MapBlower;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class MapReference<T> extends Reference<MapBlower<T>> {

    public Class<T> clazz;

    public MapReference(Context context, String path, long blowerCreation, MapBlower<T> blower, Class<T> clazz, Long moment) {
        super(context, path, moment);
        blowerMap = new HashMap<>();
        blowerMap.put(blowerCreation, blower);
        this.clazz = clazz;
    }

    @Override
    public void blowerResult(String value) {
        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(value, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Long, MapBlower<T>> entry : blowerMap.entrySet()) {
            entry.getValue().onMapChanged(map);
        }
    }

    @Override
    public void addBlower(long blowerCreation, MapBlower<T> blower) {
        blowerMap.put(blowerCreation, blower);
    }

    @Override
    public String getStringReference() {
        String val;
        if (getLastest().updateMap() == null) {
            if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                val = stringReference;
            } else {
                val = EMPTY_OBJECT;
            }
        } else {
            val = gson.toJson(getLastest().updateMap(), getType(clazz));
        }

        /*
        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(val, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        for (MapBlower<T> blower : blowerList) {
            blower.onMapChanged(map);
        }*/
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

        for (Map.Entry<Long, MapBlower<T>> entry : blowerMap.entrySet()) {
            entry.getValue().onMapChanged(map);
        }
    }

    private MapBlower<T> getLastest() {
        long lastest = 0;
        MapBlower<T> blower = null;
        for (Map.Entry<Long, MapBlower<T>> entry : blowerMap.entrySet()) {
            if (lastest < entry.getKey()) {
                lastest = entry.getKey();
                blower = entry.getValue();
            }
        }
        return blower;
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
        TypeToken t = new TypeToken<Map<String, T>>() {}.where(new TypeParameter<T>() {}, type);
        return t.getType();
    }
}
