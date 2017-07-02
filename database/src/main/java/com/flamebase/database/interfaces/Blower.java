package com.flamebase.database.interfaces;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface Blower<T> {

    void progress(int value);
    String getTag();

}
