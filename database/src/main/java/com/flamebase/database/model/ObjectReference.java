package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.ObjectBlower;
import com.google.common.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class ObjectReference<T> extends Reference<ObjectBlower<T>> {

    public Class<T> clazz;

    public ObjectReference(Context context, String path, long blowerCreation, ObjectBlower<T> blower, Class<T> clazz, Long moment) {
        super(context, path, moment);
        blowerMap = new HashMap<>();
        blowerMap.put(blowerCreation, blower);
        this.clazz = clazz;
    }

    /**
     * called when object is synchronized with cluster or path
     * not exists in local db and is stored (cached references)
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
            blowerResult(stringReference);
        }
    }

    private ObjectBlower<T> getLastest() {
        long lastest = 0;
        ObjectBlower<T> blower = null;
        // TODO limit list of blowers
        for (Map.Entry<Long, ObjectBlower<T>> entry : blowerMap.entrySet()) {
            if (lastest < entry.getKey()) {
                lastest = entry.getKey();
                blower = entry.getValue();
            }
        }
        return blower;
    }

}
