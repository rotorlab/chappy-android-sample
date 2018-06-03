package com.rotor.chappy.activities.chat_detail;

import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;

public class ChatDetailPresenter implements ChatDetailInterface.Presenter<Chat>, ProfilePresenter {

    private ChatDetailInterface.View<Chat> view;
    private ProfilesView viewProfiles;
    private ProfileRepository profileRepository;
    private ChatRepository chatRepository;
    private boolean visible;

    public ChatDetailPresenter(ChatDetailInterface.View<Chat> view, ProfilesView viewProfiles) {
        this.view = view;
        this.viewProfiles = viewProfiles;
        this.chatRepository = new ChatRepository();
        this.profileRepository = new ProfileRepository();
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
}
