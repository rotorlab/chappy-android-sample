package com.rotor.chappy.model.mpv;

import com.rotor.chappy.model.User;

public interface ProfilesView {

    void onCreateUser(String key);

    void onUserChanged(String key, User user);

    User onUpdateUser(String key);

    void onDestroyUser(String key);

    void userProgress(String key, int value);

}
