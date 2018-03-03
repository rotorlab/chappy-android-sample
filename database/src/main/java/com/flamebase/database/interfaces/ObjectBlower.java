package com.flamebase.database.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface ObjectBlower <T> extends Blower<T> {

    @Nullable T updateObject();

    void onObjectChanged(@NonNull T ref);

}
