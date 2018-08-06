package com.rotor.chappy.fragments.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.App;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.query.QueryUsersMap;
import com.rotor.chappy.model.query.ResponseUsersMap;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;
import com.rotor.database.interfaces.QueryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapPresenter implements MapInterface.Presenter {

    private MapFragment view;
    private FirebaseAuth mAuth;
    private HashMap<String, User> users;
    private ArrayList<String> asked;

    public MapPresenter(MapFragment view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        users = new HashMap<>();
        asked = new ArrayList<>();
    }

    @Override
    public void start() {
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO add logout
        } else {
            QueryUsersMap query = new QueryUsersMap(user.getUid());
            ResponseUsersMap.UsersMapChat mask = new ResponseUsersMap.UsersMapChat();
            Database.query(App.databaseName,"/chats/*", query, mask, new QueryCallback<ResponseUsersMap>() {
                @Override
                public void response(ResponseUsersMap response) {
                    for (ResponseUsersMap.UsersMapChat chat : response.getChats()) {
                        for (Map.Entry<String, ResponseUsersMap.UsersMapChat.Member> entry : chat.getMembers().entrySet()) {
                            if (!asked.contains(entry.getValue().getUserId())) {
                                asked.add(entry.getValue().getUserId());
                                listenUser(entry.getValue().getUserId());
                            }
                        }
                    }
                }

            }, ResponseUsersMap.class);
        }
    }

    @Override
    public void listenUser(final String id) {
        Database.listen(App.databaseName, "/users/" + id, new Reference<User>(User.class) {

            @Override
            public void onCreate() {
                // nothing to do here
            }

            @Override
            public void onChanged(@NonNull User ref) {
                users.put(ref.getUid(), ref);
                view.updateUI();
            }

            @Nullable
            @Override
            public User onUpdate() {
                if (users.containsKey(id)) {
                    return users.get(id);
                }
                return null;
            }

            @Override
            public void onDestroy() {
                users.remove(id);
            }

            @Override
            public void progress(int value) {
                // nothing to do here
            }

        });
    }

    @Override
    public HashMap<String, User> users() {
        return users;
    }
}
