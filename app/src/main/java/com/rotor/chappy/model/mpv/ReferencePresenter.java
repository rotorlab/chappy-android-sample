package com.rotor.chappy.model.mpv;

public interface ReferencePresenter<T> extends BasePresenter {

    void prepareFor(String id, Class<T> clazz);

    void sync(String id);

    void remove(String id);

}
