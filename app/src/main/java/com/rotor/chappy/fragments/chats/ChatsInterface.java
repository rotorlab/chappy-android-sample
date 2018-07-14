package com.rotor.chappy.fragments.chats;

import com.rotor.chappy.model.Chat;

import java.util.HashMap;

public interface ChatsInterface {

    interface Presenter {

        void start();

        void goToChat(Chat chat);

        void createChat(Chat chat);

        void listenChat(String id);

        void updateChat(Chat chat);

        HashMap<String, Chat> chats();

    }

    interface View {

        void openChat(Chat chat);

        void chatCreated();

        void chatChanged(Chat chat);

        Chat getChat(String id);

        void chatDeleted(Chat chat);

    }

}
