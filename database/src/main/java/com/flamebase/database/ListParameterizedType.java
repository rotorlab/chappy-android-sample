package com.flamebase.database;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by efraespada on 21/05/2017.
 */

public class ListParameterizedType<T> implements ParameterizedType {

    private Class<T> clazz;
    public ListParameterizedType(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[0];
    }

    @Override
    public Type getRawType() {
        return clazz;
    }

    @Override
    public Type getOwnerType() {
        return clazz;
    }

}
