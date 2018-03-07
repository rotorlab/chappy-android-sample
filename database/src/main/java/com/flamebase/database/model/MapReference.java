package com.flamebase.database.model;

import android.content.Context;

import com.flamebase.database.ReferenceUtils;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.mods.KotlinMapBlower;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class MapReference<T> extends Reference<MapBlower<T>> {

    private Class<T> clazz;

    public MapReference(Context context, String path, long blowerCreation, MapBlower<T> blower, Class<T> clazz, Long moment) {
        super(context, path, moment);
        blowerMap.put(blowerCreation, blower);
        this.clazz = clazz;
    }

    @Override
    public void blowerResult(String value) {
        Map<String, T> map = new HashMap<>();
        LinkedTreeMap<String, T> mapTemp = gson.fromJson(value, getType());
        for (LinkedTreeMap.Entry<String, T> entry : mapTemp.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Long, MapBlower<T>> entry : blowers().entrySet()) {
            if (entry.getValue() instanceof KotlinMapBlower) {
                ((KotlinMapBlower) entry.getValue()).source(value);
            } else {
                entry.getValue().onMapChanged(map);
            }
        }
    }

    @Override
    public void addBlower(long blowerCreation, MapBlower<T> blower) {
        blowerMap.put(blowerCreation, blower);
    }

    @Override
    public String getStringReference() {
        String val;
        if (getLastest() instanceof KotlinMapBlower) {
            if (((KotlinMapBlower) getLastest()).string() == null) {
                if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                    val = stringReference;
                } else {
                    val = EMPTY_OBJECT;
                }
            } else {
                val = ((KotlinMapBlower) getLastest()).string();
            }
        } else {
            if (getLastest().updateMap() == null) {
                if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
                    val = stringReference;
                } else {
                    val = EMPTY_OBJECT;
                }
            } else {
                val = gson.toJson(getLastest().updateMap(), getType());
            }
        }
        return val;
    }

    @Override
    public void loadCachedReference() {
        stringReference = ReferenceUtils.getElement(path);
        if (stringReference != null && stringReference.length() > EMPTY_OBJECT.length()) {
            blowerResult(stringReference);
        }
    }

    @Override
    public MapBlower<T> getLastest() {
        long lastest = 0;
        MapBlower<T> blower = null;
        for (Map.Entry<Long, MapBlower<T>> entry : blowers().entrySet()) {
            if (lastest < entry.getKey()) {
                lastest = entry.getKey();
                blower = entry.getValue();
            }
        }
        return blower;
    }

    public <T> Type getType() {
        TypeToken t = new TypeToken<Map<String, T>>() {}.where(new TypeParameter<T>() {}, (Class<T>) clazz);
        return t.getType();
    }
}
