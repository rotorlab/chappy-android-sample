package com.rotor.chappy.activities.profile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.services.ProfileRepository;

public class ProfilePresenter implements ProfileInterface.Presenter, com.rotor.chappy.model.mpv.ProfilePresenter {

    private ProfileInterface.View view;
    private ProfileRepository profileRepository;
    private FirebaseAuth mAuth;
    private boolean visible;

    public ProfilePresenter(ProfileInterface.View view) {
        this.view = view;
        this.profileRepository = new ProfileRepository();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO logout
        } else {
            prepareProfileFor("/users/" + user.getUid());
        }
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

    @Override
    public String getLoggedUid() {
        return mAuth != null && mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }
}
