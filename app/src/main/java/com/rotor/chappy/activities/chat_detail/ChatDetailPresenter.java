package com.rotor.chappy.activities.chat_detail;

import com.rotor.chappy.services.Data;

public class ChatDetailPresenter<T> implements ChatDetailInterface.Presenter {

    private ChatDetailInterface.View<T> view;
    private Data data;
    private boolean visible;

    public ChatDetailPresenter(ChatDetailInterface.View<T> view) {
        this.view = view;
        this.data = new Data();
    }

    @Override
    public void prepareFor(String path, Class clazz) {
        data.listen(path, this, view, clazz);
    }

    @Override
    public void sync(String id) {
        data.sync(id);
    }

    @Override
    public void remove(String id) {
        data.remove(id);
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
