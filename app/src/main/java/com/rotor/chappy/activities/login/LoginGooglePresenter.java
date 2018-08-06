package com.rotor.chappy.activities.login;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ProfileRepository;

public class LoginGooglePresenter implements LoginGoogleInterface.Presenter<User> {

    private LoginGoogleInterface.View<User> view;
    private ProfileRepository profileRepository;
    private boolean visible;
    private FirebaseAuth mAuth;

    public LoginGooglePresenter(LoginGoogleInterface.View<User> view) {
        this.view = view;
        this.profileRepository = new ProfileRepository();
        this.mAuth = FirebaseAuth.getInstance();
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
    public void sayHello(User user) {
        view.sayHello(user);
    }

    @Override
    public void goMain() {
        view.goMain();
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
