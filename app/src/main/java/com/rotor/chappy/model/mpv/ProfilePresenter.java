package com.rotor.chappy.model.mpv;

public interface ProfilePresenter extends BasePresenter {

    void prepareProfileFor(String id);

    void syncProfile(String id);

    void removeProfile(String id);

    String getLoggedUid();

}
