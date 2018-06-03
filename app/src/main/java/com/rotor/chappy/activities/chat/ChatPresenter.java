package com.rotor.chappy.activities.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;

public class ChatPresenter<T> implements ChatInterface.Presenter, ProfilePresenter {

    private ChatInterface.View<T> view;
    private ProfilesView viewProfiles;
    private ChatRepository chatRepository;
    private ProfileRepository profileRepository;
    private FirebaseAuth mAuth;
    private boolean visible;

    public ChatPresenter(ChatInterface.View<T> view, ProfilesView viewProfiles) {
        this.view = view;
        this.viewProfiles = viewProfiles;
        this.chatRepository = new ChatRepository();
        this.profileRepository = new ProfileRepository();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void prepareFor(String path, Class clazz) {
        chatRepository.listen(path, this, view, clazz);
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
