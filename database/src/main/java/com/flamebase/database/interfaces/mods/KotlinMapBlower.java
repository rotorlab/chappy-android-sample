package com.flamebase.database.interfaces.mods;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.flamebase.database.interfaces.MapBlower;

import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class KotlinMapBlower<T> implements MapBlower<T> {

    public abstract @Nullable String string();

    public abstract @Nullable void source(String value);

    @Nullable
    @Override
    public Map<String, T> updateMap() {
        return null;
    }

    @Override
    public void onMapChanged(@NonNull Map<String, T> ref) {
        // nothing to do here
    }

}