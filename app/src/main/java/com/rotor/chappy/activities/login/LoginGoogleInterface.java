package com.rotor.chappy.activities.login;

import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfileView;

public interface LoginGoogleInterface {

    interface Presenter<T> extends ProfilePresenter {

        void sayHello(T user);

        void goMain();

    }

    interface View<T> extends ProfileView {

        void sayHello(T user);

        void goMain();

    }
}
