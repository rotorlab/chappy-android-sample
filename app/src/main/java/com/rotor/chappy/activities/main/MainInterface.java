package com.rotor.chappy.activities.main;

import com.rotor.chappy.model.BasePresenter;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.MapReferenceView;
import com.rotor.chappy.model.ReferencePresenter;
import com.rotor.chappy.model.ReferenceView;
import com.rotor.chappy.model.User;

import java.util.List;
import java.util.Map;

public interface MainInterface {

    interface Presenter<T> extends ReferencePresenter<T> {

        void prepareChatsFor();

        T createChat(String name);

        void goToChat(Chat chat);

        void onResumeView();

        void onPauseView();

        void refreshUI();

    }

    interface View<T> extends MapReferenceView<T> {

        void openChat(Chat chat);

        void refreshUI();

    }
}
