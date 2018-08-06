package com.rotor.chappy.fragments.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.App;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatPresenter implements ChatInterface.Presenter {

    private ChatFragment view;
    private FirebaseAuth mAuth;
    private HashMap<String, User> users;
    private ArrayList<String> askedUsers;
    private Chat chat;

    public ChatPresenter(ChatFragment view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.users = new HashMap<>();
        this.askedUsers = new ArrayList<>();
    }

    @Override
    public void start() {
        if (App.getCurrentChat() != null) {
            listenChat();
        }
    }

    @Override
    public void listenChat() {
        Database.listen("database", "/chats/" + App.getCurrentChat(), new Reference<Chat>(Chat.class) {

            @Override
            public void onCreate() {
                // nothing to do here
            }

            @Override
            public void onChanged(@NonNull Chat ref) {
                chat = ref;
                for (Member member : chat.getMembers().values()) {
                    if (!askedUsers.contains(member.getId())) {
                        askedUsers.add(member.getId());
                        listenUser(member.getId());
                    }
                }
                view.updateUI(chat);
                view.adapter.notifyDataSetChanged();
            }

            @Nullable
            @Override
            public Chat onUpdate() {
                return chat;
            }

            @Override
            public void onDestroy() {
                view.chatDeleted();
                chat = null;
            }

            @Override
            public void progress(int value) {
                // nothing to do here
            }

        });
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
                view.adapter.notifyDataSetChanged();
            }

            @Nullable
            @Override
            public User onUpdate() {
                if (users.containsKey(id)) return users.get(id);
                return null;
            }

            @Override
            public void onDestroy() {
                if (users.containsKey(id)) users.remove(id);
            }

            @Override
            public void progress(int value) {
                // nothing to do here
            }

        });
    }

    @Override
    public void updateChat() {
        Database.sync("/chats/" + chat.getId());
    }

    @Override
    public Chat chat() {
        return chat;
    }

    @Override
    public HashMap<String, User> users() {
        return users;
    }

    @Override
    public FirebaseAuth getUser() {
        return mAuth;
    }

    @Override
    public void remove() {
        Database.remove("/chats/" + chat.getId());
    }

}
