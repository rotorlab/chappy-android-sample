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

        void askGroupName();

    }

    interface View {

        void openChat(Chat chat);
        
        void chatChanged(Chat chat);

        void askGroupName();

    }

}
