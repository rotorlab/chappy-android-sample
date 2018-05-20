package com.rotor.chappy.activities.splash;

import com.rotor.chappy.model.BasePresenter;
import com.rotor.chappy.model.ReferenceView;

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
