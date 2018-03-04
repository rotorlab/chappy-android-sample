package com.flamebase.database.interfaces.mods;

import android.support.annotation.Nullable;

import com.flamebase.database.interfaces.ObjectBlower;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class KotlinObjectBlower<T> implements ObjectBlower<T> {

    public abstract @Nullable String string();

    @Nullable
    @Override
    public T updateObject() {
        return null;
    }
}
