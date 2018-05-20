package com.rotor.chappy.activities.login;

import com.rotor.chappy.model.User;
import com.rotor.chappy.services.Data;

public class LoginGooglePresenter implements LoginGoogleInterface.Presenter<User> {

    private LoginGoogleInterface.View<User> view;
    private Data data;
    private boolean visible;

    public LoginGooglePresenter(LoginGoogleInterface.View<User> view) {
        this.view = view;
        this.data = new Data();
    }

    @Override
    public void prepareFor(String id, Class clazz) {
        data.listen(id, this, view, clazz);
    }

    @Override
    public void sync(String id) {
        data.sync(id);
    }

    @Override
    public void remove(String id) {
        data.remove(id);
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
