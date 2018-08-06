package com.rotor.chappy.activities.chat_detail;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.App;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.HashMap;
import java.util.Map;

public class ChatDetailPresenter implements ChatDetailInterface.Presenter {

    private ChatDetailInterface.View<Chat> view;
    private boolean visible;
    private FirebaseAuth mAuth;
    private Chat chat;
    private HashMap<String, User> users;
    private HashMap<String, Member> members;

    public ChatDetailPresenter(ChatDetailInterface.View<Chat> view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.members = new HashMap<>();
        this.users = new HashMap<>();
    }

    @Override
    public void start(final Intent intent) {
        if (intent.hasExtra("path")) {
            Database.listen(App.databaseName, "/chats/" + intent.getStringExtra("path"), new Reference<Chat>(Chat.class) {
                @Override
                public void onCreate() {
                    // shouldn't be called
                }
                @Override
                public void onChanged(@NonNull Chat ref) {
                    chat = ref;
                    for (Map.Entry<String, Member> entry : chat.getMembers().entrySet()) {
                        if (!members.containsKey(entry.getValue().getId())) {
                            members.put(entry.getValue().getId(), entry.getValue());
                            listenUser(entry.getValue().getId());
                        }
                    }
                }
                @Nullable
                @Override
                public Chat onUpdate() {
                    return chat;
                }
                @Override
                public void progress(int value) {
                    // nothing to do here
                }
                @Override
                public void onDestroy() {
                    chat = null;
                    Database.unlisten("/chats/" + intent.getStringExtra("path"));
                }
            });
        }
    }

    @Override
    public Chat chat() {
        return chat;
    }

    @Override
    public Map<String, User> users() {
        return users;
    }

    @Override
    public Map<String, Member> members() {
        return members;
    }

    @Override
    public void listenUser(final String id) {
        Database.listen(App.databaseName, "/users/" + id, new Reference<User>(User.class) {
            @Override
            public void onCreate() {
                // shouldn't be called
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
            public void progress(int value) {
                // nothing to do here
            }
            @Override
            public void onDestroy() {
                if (users.containsKey(id)) {
                    users.remove(id);
                }
            }
        });
    }

    @Override
    public String userId() {
        return mAuth.getUid();
    }

    @Override
    public void sync() {
        Database.sync("/chats/" + chat.getId());
    }
}