package com.rotor.chappy.fragments.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.rotor.chappy.App;
import com.rotor.chappy.fragments.chat.ChatFragment;
import com.rotor.chappy.fragments.chats.ChatsFragment;
import com.rotor.chappy.fragments.chats.ChatsInterface;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;
import com.rotor.database.interfaces.QueryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            Database.query(App.databaseName,"/chats/*",
                    "{\"members\": { \"*\": { \"id\": \"" + user.getUid() + "\" } } }",
                    "{ \"id\": \"\"," +
                            "\"members\": {" +
                                "\"*\": {" +
                                    "\"id\": \"\"" +
                                "}" +
                            "} }",
                    new QueryCallback() {

                        @Override
                        public void response(List<LinkedTreeMap<String, String>> list) {
                            for(LinkedTreeMap m : list) {
                                LinkedTreeMap c = (LinkedTreeMap) m.get("members");
                                Member[] members = (Member[]) c.values().toArray(new Member[0]);
                                for (Member member : members) {
                                    if (!asked.contains(member.getId())) {
                                        asked.add(member.getId());
                                        listenUser(member.getId());
                                    }
                                }
                            }
                        }

                    });
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
