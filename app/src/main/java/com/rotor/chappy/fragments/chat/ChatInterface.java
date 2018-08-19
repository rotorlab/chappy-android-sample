package com.rotor.chappy.fragments.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;

import java.util.HashMap;

public interface ChatInterface {

    interface Presenter {

        void start();

        void stop();

        void listenChat();

        void listenUser(String id);

        void updateChat();

        Chat chat();

        void remove();

        HashMap<String, User> users();

        FirebaseAuth getUser();

    }

    interface View {

        void updateUI(Chat chat);

        void chatDeleted();

    }

}
