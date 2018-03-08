package com.flamebase.database.interfaces.mods;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.flamebase.database.interfaces.ObjectBlower;

import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public abstract class KotlinObjectBlower<T> implements ObjectBlower<T> {

    public abstract @Nullable String string();

    public abstract @Nullable void source(String value);

    @Nullable
    @Override
    public T onUpdate() {
        return null;
    }

    @Override
    public void onChanged(@NonNull T ref) {
        // nothing to do here
    }

}
