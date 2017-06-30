package com.flamebase.database.interfaces;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface ObjectBlower <T> extends Blower<T> {

    T updateObject();
    void onObjectChanged(T ref);

}
