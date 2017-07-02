package com.flamebase.database.interfaces;

import java.util.List;

/**
 * Created by efraespada on 29/06/2017.
 */

public interface ListBlower<T> extends Blower<T> {

    List<T> updateList();
    void onListChanged(List<T> ref);

}