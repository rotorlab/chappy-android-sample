package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class ObjectReference<T> extends Reference<ObjectBlower<T>> {

    public Class<T> clazz;

    public ObjectReference(Context context, String path, long blowerCreation, ObjectBlower<T> blower, Class<T> clazz) {
        super(context, path);
        blowerMap = new HashMap<>();
        blowerMap.put(blowerCreation, blower);
        this.clazz = clazz;
    }

    public ObjectReference(Context context, String path, long blowerCreation, ObjectBlower<T> blower, Class<T> clazz, RemoteMessage remoteMessage) {
        super(context, path, remoteMessage);
        blowerMap = new HashMap<>();
        blowerMap.put(blowerCreation, blower);
        this.clazz = clazz;
    }

    /**
     * called when object is synchronized with cluster or path
     * not exists in local db and is stored (cached references)
     *
     * @return T object
     */
    @Override
    public void addBlower(long creation, ObjectBlower<T> blower) {
        blowerMap.put(creation, blower);
    }

    @Override
    public String getStringReference() {
        String val;
        if (getLastest().updateObject() == null) {
            if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                val = stringReference;
            } else {
                val = EMPTY_OBJECT;
            }
        } else {
            val = gson.toJson(getLastest().updateObject(), TypeToken.of(clazz).getType());
        }

        return val;
    }

    @Override
    public void blowerResult(String value) {
        for (Map.Entry<Long, ObjectBlower<T>> entry : blowerMap.entrySet()) {
            entry.getValue().onObjectChanged((T) gson.fromJson(value, TypeToken.of(clazz).getType()));
        }
    }

    @Override
    public void loadCachedReference() {
        stringReference = ReferenceUtils.getElement(path);
        if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
            for (Map.Entry<Long, ObjectBlower<T>> entry : blowerMap.entrySet()) {
                entry.getValue().onObjectChanged((T) gson.fromJson(stringReference, TypeToken.of(clazz).getType()));
            }
        } else {
            // blower.onObjectChanged(null);
        }
    }

    private ObjectBlower<T> getLastest() {
        long lastest = 0;
        ObjectBlower<T> blower = null;
        for (Map.Entry<Long, ObjectBlower<T>> entry : blowerMap.entrySet()) {
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
