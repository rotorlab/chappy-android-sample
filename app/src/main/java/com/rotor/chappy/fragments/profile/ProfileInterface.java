package com.rotor.chappy.fragments.profile;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;

import java.util.HashMap;

public interface ProfileInterface {

    interface Presenter {

        void start();

        void listenUser(String id);

        void updateUser();

        User user();

        FirebaseAuth getUser();

    }

    interface View {

        void userUpdated();

    }

}
