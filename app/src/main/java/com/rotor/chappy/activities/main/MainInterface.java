package com.rotor.chappy.activities.main;

import com.rotor.chappy.model.BasePresenter;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;

import java.util.List;

public interface MainInterface {

    interface Presenter extends BasePresenter {

        void prepareChatsFor();

        void createChat(String name);

        void goToChat(Chat chat);

        void onResumeView();

        void onPauseView();

    }

    interface View {

        void openChat(Chat chat);

        void refresh(List<Chat> chats);

    }
}
