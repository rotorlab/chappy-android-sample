package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.ObjectBlower;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class ObjectReference<T> extends Reference {

    public ObjectBlower<T> blower;
    public Class<T> clazz;

    public ObjectReference(Context context, String path, ObjectBlower<T> blower, Class<T> clazz) {
        super(context, path);
        this.blower = blower;
        this.clazz = clazz;
    }

    public ObjectReference(Context context, String path, ObjectBlower<T> blower, Class<T> clazz, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        this.blower = blower;
        this.clazz = clazz;
    }

    /**
     * called when object is synchronized with cluster or path
     * not exists in local db and is stored (cached references)
     *
     * @return T object
     */
    public abstract T updateObject();

    @Override
    public String getStringReference() {
        String val;
        if (updateObject() == null) {
            val = stringReference;
        } else {
            val = gson.toJson(updateObject(), TypeToken.of(clazz).getType());
        }

        return val;
    }

    @Override
    public void blowerResult(String value) {
        if (!isSynchronized) {
            isSynchronized = true;
        }
        blower.onObjectChanged((T) gson.fromJson(value, TypeToken.of(clazz).getType()));
    }

    @Override
    public void loadCachedReference() {
        stringReference = ReferenceUtils.getElement(path);
        if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
            blower.onObjectChanged((T) gson.fromJson(stringReference, TypeToken.of(clazz).getType()));
        } else {
            blower.onObjectChanged(null);
        }
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
            return new Type[]{wrapped};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }

    }

}
