package com.rotor.chappy.model.mpv;

public interface MapReferenceView<T> {

    void onCreateReference(String key);

    void onReferenceChanged(String key, T chat);

    T onUpdateReference(String key);

    void onDestroyReference(String key);

    void progress(String key, int value);

}
