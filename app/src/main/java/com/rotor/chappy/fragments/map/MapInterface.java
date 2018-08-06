package com.rotor.chappy.fragments.map;

import com.rotor.chappy.model.Location;
import com.rotor.chappy.model.User;

import java.util.HashMap;

public interface MapInterface {

    interface Presenter {

        void start();

        void listenUser(String id);

        HashMap<String, User> users();

    }

    interface View {

        void updateUI();

        void goToPosition(Location location);

    }

}
