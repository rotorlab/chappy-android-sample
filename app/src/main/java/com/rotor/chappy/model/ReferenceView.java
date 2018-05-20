package com.rotor.chappy.model;

public interface ReferenceView<T> {

    void onCreateReference();

    void onReferenceChanged(T chat);

    T onUpdateReference();

    void onDestroyReference();

    void progress(int value);

}
