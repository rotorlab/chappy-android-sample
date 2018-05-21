package com.rotor.chappy.activities.main;

import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.mpv.MapReferenceView;
import com.rotor.chappy.model.mpv.ReferencePresenter;

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
