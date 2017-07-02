package com.flamebase.database.interfaces;

import java.util.Map;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface MapBlower<T> extends Blower<T> {

    Map<String, T> updateMap();
    void onMapChanged(Map<String, T> ref);

}