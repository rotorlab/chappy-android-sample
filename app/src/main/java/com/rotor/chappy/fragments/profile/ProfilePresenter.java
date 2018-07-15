package com.rotor.chappy.fragments.profile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.App;
import com.rotor.chappy.fragments.chat.ChatFragment;
import com.rotor.chappy.fragments.chat.ChatInterface;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfilePresenter implements ProfileInterface.Presenter {

    private ProfileFragment view;
    private FirebaseAuth mAuth;
    private User user;

    public ProfilePresenter(ProfileFragment view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO add logout
        } else {
            listenUser(user.getUid());
        }
    }

    @Override
    public void listenUser(final String id) {
        Database.listen("database", "/users/" + id, new Reference<User>(User.class) {

            @Override
            public void onCreate() {
                // nothing to do here
            }

            @Override
            public void onChanged(@NonNull User ref) {
                user = ref;
                view.userUpdated();
            }

            @Nullable
            @Override
            public User onUpdate() {
                return user;
            }

            @Override
            public void onDestroy() {
                user = null;
            }

            @Override
            public void progress(int value) {
                // nothing to do here
            }

        });
    }

    @Override
    public void updateUser() {
        Database.sync("/users/" + user.getUid());
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public FirebaseAuth getUser() {
        return mAuth;
    }
}
