package com.rotor.chappy.activities.splash;

public interface SplashInterface {

    interface Presenter {

        void start();

        void goLogin();

        void goMain();

    }

    interface View {

        void goLogin();

        void goMain();

    }
}
