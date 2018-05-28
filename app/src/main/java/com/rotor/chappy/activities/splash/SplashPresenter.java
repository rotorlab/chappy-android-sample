package com.rotor.chappy.activities.splash;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfileView;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;

public class SplashPresenter implements SplashInterface.Presenter, ProfilePresenter {

    private SplashInterface.View view;
    private ProfileRepository profileRepository;
    private FirebaseAuth mAuth;
    private boolean visible;

    public SplashPresenter(SplashInterface.View view) {
        this.view = view;
        this.profileRepository = new ProfileRepository();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            view.goLogin();
        } else {
            prepareProfileFor("/users/" + user.getUid());
        }
    }

    @Override
    public void goLogin() {
        view.goLogin();
    }

    @Override
    public void goMain() {
        view.goMain();
    }

    @Override
    public void onResumeView() {
        visible = true;
    }

    @Override
    public void onPauseView() {
        visible = false;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void prepareProfileFor(String id) {
        profileRepository.listen(id, this, view);
    }

    @Override
    public void syncProfile(String id) {
        profileRepository.sync(id);
    }

    @Override
    public void removeProfile(String id) {
        profileRepository.remove(id);
    }
}
