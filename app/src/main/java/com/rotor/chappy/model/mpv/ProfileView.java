package com.rotor.chappy.model.mpv;

import com.rotor.chappy.model.User;

public interface ProfileView {

    void onCreateUser();

    void onUserChanged(User user);

    User onUpdateUser();

    void onDestroyUser();

    void userProgress(int value);

}
