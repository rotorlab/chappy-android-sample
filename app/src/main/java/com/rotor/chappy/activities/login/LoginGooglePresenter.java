package com.rotor.chappy.activities.login;

import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ChatRepository;

public class LoginGooglePresenter implements LoginGoogleInterface.Presenter<User> {

    private LoginGoogleInterface.View<User> view;
    private ChatRepository chatRepository;
    private boolean visible;

    public LoginGooglePresenter(LoginGoogleInterface.View<User> view) {
        this.view = view;
        this.chatRepository = new ChatRepository();
    }

    @Override
    public void prepareFor(String id, Class clazz) {
        chatRepository.listen(id, this, view, clazz);
    }

    @Override
    public void sync(String id) {
        chatRepository.sync(id);
    }

    @Override
    public void remove(String id) {
        chatRepository.remove(id);
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
}
