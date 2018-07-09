package com.rotor.chappy.activities.home;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.enums.FragmentType;


public class HomePresenter implements HomeInterface.Presenter {

    private HomeActivity view;
    private FirebaseAuth mAuth;

    public HomePresenter(HomeActivity view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {

    }

    @Override
    public void goTo(FragmentType fragmentType) {
        view.goTo(fragmentType);
    }

    @Override
    public boolean isLogged() {
        return mAuth != null && mAuth.getCurrentUser() != null;
    }

    @Override
    public void logout() {
        view.logout();
    }
}
