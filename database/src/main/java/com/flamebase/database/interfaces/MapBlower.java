package com.flamebase.database.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface MapBlower<T> extends Blower<T> {

    @Nullable Map<String, T> updateMap();

    void onMapChanged(@NonNull Map<String, T> ref);

}