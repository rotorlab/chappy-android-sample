package com.rotor.chappy.activities.splash;

import com.rotor.chappy.model.mpv.BasePresenter;
import com.rotor.chappy.model.mpv.ReferenceView;

public interface SplashInterface {

    interface Presenter extends BasePresenter {

        void start();

        void goLogin();

        void goMain();

    }

    interface View<T> extends ReferenceView<T> {

        void goLogin();

        void goMain();

    }
}
