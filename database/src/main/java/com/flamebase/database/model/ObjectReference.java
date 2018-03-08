package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.ObjectBlower;
import com.flamebase.database.interfaces.mods.KotlinObjectBlower;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class ObjectReference<T> extends Reference<ObjectBlower<T>> {

    private Class<T> clazz;

    public ObjectReference(Context context, String path, long blowerCreation, ObjectBlower<T> blower, Class<T> clazz, Long moment) {
        super(context, path, moment);
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
        if (getLastest() instanceof KotlinObjectBlower) {
            if (((KotlinObjectBlower) getLastest()).string() == null) {
                if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                    val = stringReference;
                } else {
                    val = EMPTY_OBJECT;
                }
            } else {
                val = ((KotlinObjectBlower) getLastest()).string();
            }
        } else {
            if (getLastest().onUpdate() == null) {
                if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                    val = stringReference;
                } else {
                    val = EMPTY_OBJECT;
                }
            } else {
                val = gson.toJson(getLastest().onUpdate(), TypeToken.of(clazz).getType());
            }
        }

        return val;
    }

    @Override
    public void blowerResult(String value) {
        for (Map.Entry<Long, ObjectBlower<T>> entry : blowers().entrySet()) {
            if (entry.getValue() instanceof KotlinObjectBlower) {
                ((KotlinObjectBlower) entry.getValue()).source(value);
            } else {
                entry.getValue().onChanged((T) gson.fromJson(value, getType()));
            }
        }
    }

    @Override
    public void loadCachedReference() {
        stringReference = ReferenceUtils.getElement(path);
        if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
            blowerResult(stringReference);
        }
    }

    @Override
    public ObjectBlower<T> getLastest() {
        long lastest = 0;
        ObjectBlower<T> blower = null;
        // TODO limit list of blowers
        for (Map.Entry<Long, ObjectBlower<T>> entry : blowers().entrySet()) {
            if (lastest < entry.getKey()) {
                lastest = entry.getKey();
                blower = entry.getValue();
            }
        }
        return blower;
    }

    public <T> Type getType() {
        return TypeToken.of((Class<T>) clazz).getType();
    }

}
