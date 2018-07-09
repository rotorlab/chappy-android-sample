package com.rotor.chappy.activities.home;

import com.rotor.chappy.enums.FragmentType;

public interface HomeInterface {

    interface Presenter {

        void start();

        void goTo(FragmentType fragmentType);

        boolean isLogged();

        void logout();

    }

    interface View {

        void goTo(FragmentType fragmentType);

        void logout();

    }
}
