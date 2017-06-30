package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ListParameterizedType;
import com.flamebase.database.interfaces.Blower;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;
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

public abstract class ObjectReference<T> extends Reference {

    public T reference = null;
    public Blower blower;
    public Gson gson;
    public Type type;


    public ObjectReference(Context context, String path, ObjectBlower<T> blower, Type type) {
        super(context, path);
        this.blower = blower;
        this.type = type;
        this.gson = new Gson();
    }

    public ObjectReference(Context context, String path, ObjectBlower<T> blower, Type type, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        this.blower = blower;
        this.type = type;
        this.gson = new Gson();
    }

    public abstract T updateObject();

    public abstract void onObjectChanged(T ref);

    @Override
    public void loadCachedReference() {
        stringReference = getElement(path);
        if (stringReference != null) {
            reference = gson.fromJson(stringReference, type);
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
