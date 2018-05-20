package com.rotor.chappy.activities.main;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.App;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.Data;
import com.rotor.database.Database;
import com.rotor.database.interfaces.QueryCallback;

import org.json.JSONArray;

public class MainPresenter implements MainInterface.Presenter {

    private MainInterface.View view;
    private Data data;
    private boolean visible;
    private FirebaseAuth mAuth;

    public MainPresenter(MainInterface.View view) {
        this.view = view;
        this.data = new Data();
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void prepareChatsFor() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO add logout
        } else {
            Database.query(App.databaseName,"/chats/*",
                    "{\"members\": { \"" + user.getUid() + "\": { \"id\": " + user.getUid() + " } } }",
                    "{ \"id\": \"\" }",
                    new QueryCallback() {

                        @Override
                        public void response(JSONArray list) {
                            Log.e("ROTOR", "chats found: " + list.toString());
                        }
                    });
        }
    }

    @Override
    public void createChat(String name) {
        // TODO create chat
    }

    @Override
    public void goToChat(Chat chat) {
        view.openChat(chat);
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
