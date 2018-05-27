package com.rotor.chappy.activities.splash;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ChatRepository;

public class SplashPresenter implements SplashInterface.Presenter {

    private SplashInterface.View<User> view;
    private ChatRepository chatRepository;
    private FirebaseAuth mAuth;
    private boolean visible;

    public SplashPresenter(SplashInterface.View<User> view) {
        this.view = view;
        this.chatRepository = new ChatRepository();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            view.goLogin();
        } else {
            chatRepository.listen("/users/" + user.getUid(), this, view, User.class);
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
}
