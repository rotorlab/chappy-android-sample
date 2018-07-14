package com.rotor.chappy.fragments.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;

import java.util.HashMap;

public interface ChatInterface {

    interface Presenter {

        void start();

        void listenChat(String id);

        void listenUser(String id);

        void updateChat();

        Chat chat();

        HashMap<String, User> users();

        FirebaseAuth getUser();

    }

    interface View {

        void chatDeleted();

    }

}
