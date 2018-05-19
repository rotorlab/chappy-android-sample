package com.rotor.chappy.activities.chat;

public interface ChatInterface {

    interface Presenter {

    }

    interface View<T> {

        void onChatCreated();

        void onChatChanged(T chat);

        void onChatUpdate();

        void onChatDestroy();

        void onChatProgress();

    }
}
