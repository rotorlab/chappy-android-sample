package com.flamebase.database;

import android.util.AndroidRuntimeException;

/**
 * Created by efraespada on 03/03/2018.
 */

public class FlamebaseConnectionException extends AndroidRuntimeException {

    public FlamebaseConnectionException(String name) {
        super(name);
    }

}
