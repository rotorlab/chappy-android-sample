package com.rotor.chappy.activities.chat;

import com.rotor.chappy.model.mpv.ReferencePresenter;
import com.rotor.chappy.model.mpv.ReferenceView;

public interface ChatInterface {

    interface Presenter<T> extends ReferencePresenter<T> {

    }

    interface View<T> extends ReferenceView<T> {

    }
}
