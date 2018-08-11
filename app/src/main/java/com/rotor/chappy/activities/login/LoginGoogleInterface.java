package com.rotor.chappy.activities.login;

import com.rotor.chappy.model.User;

public interface LoginGoogleInterface {

    interface Presenter {

        void start();

        void goMain();

        User user();

    }

    interface View {

        void goMain();

    }
}
