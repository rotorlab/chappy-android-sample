package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.interfaces.MapBlower;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.MapTypeAdapterFactory;

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
    public Gson gson;
    public Class<T> clazz;

    public MapReference(Context context, String path, MapBlower<T> blower, Class<T> clazz) {
        super(context, path);
        this.blower = blower;
        this.clazz = clazz;
        this.gson = new Gson();
    }

    public MapReference(Context context, String path, MapBlower<T> blower, Class<T> clazz, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        this.blower = blower;
        this.clazz = clazz;
        this.gson = new Gson();
    }

    /**
     * called when map is synchronized with cluster or path
     * not exists in local db and is stored (cached references)
     * @return Map - String, T
     */
    public abstract Map<String, T> updateMap();

    @Override
    public String getStringReference() {
        if (updateMap() == null) {
            return "{}";
        } else {
            return gson.toJson(updateMap(), getType(clazz));
        }
    }

    @Override
    public void loadCachedReference() {
        stringReference = getElement(path);
        if (stringReference == null) {
            stringReference = getStringReference();
            addElement(path, stringReference);
        }

        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(stringReference, getType(clazz));
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        blower.onMapChanged(map);
    }

/*
    public <V> void loadChachedReference(MapBlower<String,V> blower, Type clazz) {
        Gson gson = new Gson();
        stringReference = getElement(path);

        if (stringReference != null) {
            Map<String, V> map = gson.fromJson(stringReference, clazz);
            blower.onMapChanged(map);
        }
    }

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

    private Gson getGsonBuilder() throws NoSuchMethodException {
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        return new GsonBuilder().registerTypeAdapter(Map.class, new MapTypeAdapterFactory(constructorConstructor, false)).create();
    }

    private static <T> Type getType(Class<T> type) {
        return new TypeToken<Map<String, T>>() {}
                .where(new TypeParameter<T>() {}, type)
                .getType();
    }
}
