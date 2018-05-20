package com.rotor.chappy.activities.login;

import com.rotor.chappy.model.ReferencePresenter;
import com.rotor.chappy.model.ReferenceView;

public interface LoginGoogleInterface {

    interface Presenter<T> extends ReferencePresenter<T> {

        void sayHello(T user);

        void goMain();

    }

    interface View<T> extends ReferenceView<T> {

        void sayHello(T user);

        void goMain();

    }
}
