package com.rotor.chappy.activities.chat_detail;

import com.rotor.chappy.model.Chat;
import com.rotor.chappy.services.ChatRepository;

public class ChatDetailPresenter implements ChatDetailInterface.Presenter<Chat> {

    private ChatDetailInterface.View<Chat> view;
    private ChatRepository chatRepository;
    private boolean visible;

    public ChatDetailPresenter(ChatDetailInterface.View<Chat> view) {
        this.view = view;
        this.chatRepository = new ChatRepository();
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
}
