package com.rotor.chappy.activities.main;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.internal.LinkedTreeMap;
import com.rotor.chappy.App;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.chappy.model.mpv.ReferenceView;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;
import com.rotor.database.Database;
import com.rotor.database.interfaces.QueryCallback;

import java.util.Date;
import java.util.List;

public class MainPresenter implements MainInterface.Presenter<Chat>, ProfilePresenter {

    private MainInterface.View<Chat> view;
    private ProfilesView viewProfiles;
    private ChatRepository chatRepository;
    private ProfileRepository profileRepository;
    private boolean visible;
    private FirebaseAuth mAuth;

    public MainPresenter(MainInterface.View<Chat> view, ProfilesView viewProfiles) {
        this.view = view;
        this.viewProfiles = viewProfiles;
        this.chatRepository = new ChatRepository();
        this.profileRepository = new ProfileRepository();
        this.mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void prepareChatsFor() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // TODO add logout
        } else {
            prepareProfileFor("/users/" + user.getUid());
            Database.query(App.databaseName,"/chats/*",
                    "{\"members\": { \"" + user.getUid() + "\": { \"id\": \"" + user.getUid() + "\" } } }",
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
        Member member = new Member();
        member.setDate(new Date().getTime());
        member.setId(ChatRepository.getUser().getUid());
        member.setRol("admin");
        chat.addMember(member);
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
        chatRepository.listen(id, this, view, clazz);
    }

    @Override
    public void sync(String id) {
        chatRepository.sync(id);
    }

    @Override
    public void remove(String id) {
        chatRepository.remove(id);
    }

    @Override
    public void prepareProfileFor(String id) {
        profileRepository.listen(id, this, viewProfiles);
    }

    @Override
    public void syncProfile(String id) {
        profileRepository.sync(id);
    }

    @Override
    public void removeProfile(String id) {
        profileRepository.remove(id);
    }

    @Override
    public String getLoggedUid() {
        return mAuth != null && mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }
}
