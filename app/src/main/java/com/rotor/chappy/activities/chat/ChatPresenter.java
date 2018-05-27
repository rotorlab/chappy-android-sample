package com.rotor.chappy.activities.chat;

import com.rotor.chappy.services.ChatRepository;

public class ChatPresenter<T> implements ChatInterface.Presenter {

    private ChatInterface.View<T> view;
    private ChatRepository chatRepository;
    private boolean visible;

    public ChatPresenter(ChatInterface.View<T> view) {
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
