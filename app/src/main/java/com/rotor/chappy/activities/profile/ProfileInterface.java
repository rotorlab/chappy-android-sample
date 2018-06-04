package com.rotor.chappy.activities.profile;

import com.rotor.chappy.model.mpv.BasePresenter;
import com.rotor.chappy.model.mpv.ProfileView;

public interface ProfileInterface {

    interface Presenter extends BasePresenter {

        void start();

    }

    interface View extends ProfileView {

    }
}
