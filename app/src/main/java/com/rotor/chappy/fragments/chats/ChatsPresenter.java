package com.rotor.chappy.fragments.chats;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.rotor.chappy.App;
import com.rotor.chappy.fragments.chat.ChatFragment;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.query.QueryId;
import com.rotor.chappy.model.query.ResponseId;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;
import com.rotor.database.interfaces.QueryCallback;

import java.util.HashMap;

public class ChatsPresenter implements ChatsInterface.Presenter {

    private ChatsFragment view;
    private FirebaseAuth mAuth;
    private HashMap<String, Chat> chats;

    public ChatsPresenter(ChatsFragment view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        chats = new HashMap<>();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO add logout
        } else {
            QueryId queryId = new QueryId(user.getUid());
            ResponseId.Id mask = new ResponseId.Id();
            Database.query(App.databaseName,"/chats/*",
                    queryId,
                    mask,
                    new QueryCallback<ResponseId>() {
                        @Override
                        public void response(ResponseId response) {
                            for (ResponseId.Id responseId : response.getIds()) {
                                listenChat(responseId.getUserId());
                            }
                        }

                    }, ResponseId.class);
        }
    }

    @Override
    public void goToChat(Chat chat) {
        App.setCurrentChat(chat.getId());
        App.setFragment(ChatFragment.class);
    }

    @Override
    public void createChat(Chat chat) {
        chats.put(chat.getId(), chat);
        listenChat(chat.getId());
    }

    @Override
    public void listenChat(final String id) {
        Database.listen("database", "/chats/" + id, new Reference<Chat>(Chat.class) {

            @Override
            public void onCreate() {
                // nothing to do here
            }

            @Override
            public void onChanged(@NonNull Chat ref) {
                chats.put(ref.getId(), ref);
                view.chatChanged(chats.get(id));
            }

            @Nullable
            @Override
            public Chat onUpdate() {
                if (chats.containsKey(id)) {
                    return chats.get(id);
                }
                return null;
            }

            @Override
            public void onDestroy() {
                chats.remove(id);
            }

            @Override
            public void progress(int value) {
                // nothing to do here
            }

        });
    }

    @Override
    public void updateChat(Chat chat) {
        chats.put(chat.getId(), chat);
        Database.sync("/chats/" + chat.getId());
    }

    @Override
    public HashMap<String, Chat> chats() {
        return chats;
    }

    @Override
    public void askGroupName() {
        view.askGroupName();
    }
}
