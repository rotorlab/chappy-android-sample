package com.rotor.chappy.activities.main;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.rotor.chappy.App;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.Data;
import com.rotor.database.Database;
import com.rotor.database.interfaces.QueryCallback;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPresenter implements MainInterface.Presenter<Chat> {

    private MainInterface.View<Chat> view;
    private Data data;
    private boolean visible;
    private FirebaseAuth mAuth;

    public MainPresenter(MainInterface.View<Chat> view) {
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
                    "{\"members\": { \"" + user.getUid() + "\": { \"uid\": \"" + user.getUid() + "\" } } }",
                    "{ \"id\": \"\" }",
                    new QueryCallback() {

                        @Override
                        public void response(List<LinkedTreeMap<String, String>> list) {
                            for(LinkedTreeMap m : list) {
                                String id = (String) m.get("id");
                                Log.e("ROTOR", "chats found: " + id);
                                prepareFor("/chats/" + id, Chat.class);
                            }
                        }

                    });
        }
    }

    @Override
    public Chat createChat(String name) {
        Chat chat = new Chat(name);
        chat.addMember(Data.getUser());
        return chat;
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
    public void refreshUI() {
        view.refreshUI();
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void prepareFor(String id, Class<Chat> clazz) {
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
}
