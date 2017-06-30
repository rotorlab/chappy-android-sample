package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.interfaces.Blower;
import com.flamebase.database.interfaces.MapBlower;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class MapReference<T> extends Reference {

    public Map<String, T> reference = null;
    public Blower blower;
    public Gson gson;


    public MapReference(Context context, String path, MapBlower<T> blower, Type type) {
        super(context, path);
        this.blower = blower;
        this.gson = new Gson();
    }

    public MapReference(Context context, String path, MapBlower<T> blower, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        this.blower = blower;
        this.gson = new Gson();
    }

    public abstract Map<String, T> updateMap();

    public abstract void onMapChanged(Map<String, T> ref);

    @Override
    public void loadCachedReference() {
        stringReference = getElement(path);
        if (stringReference != null) {
            Map<String, T> map = new HashMap<>();
            Map<String, T> mapTemp = gson.fromJson(stringReference, new TypeToken<Map>(){}.getType());
            //blower.onMapChanged(map);
        }
    }

/*
    public <V> void loadChachedReference(MapBlower<String,V> blower, Type type) {
        Gson gson = new Gson();
        stringReference = getElement(path);

        if (stringReference != null) {
            Map<String, V> map = gson.fromJson(stringReference, type);
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

    private Gson getGsonBuilder() {
        return new GsonBuilder().enableComplexMapKeySerialization().create();
    }
}
