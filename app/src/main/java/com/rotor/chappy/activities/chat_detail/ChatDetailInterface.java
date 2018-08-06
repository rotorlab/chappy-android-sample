package com.rotor.chappy.activities.chat_detail;

import android.content.Intent;

import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;

import java.util.Map;

public interface ChatDetailInterface {

    interface Presenter {

        void start(Intent intent);

        Chat chat();

        Map<String, User> users();

        Map<String, Member> members();

        void listenUser(String id);

        String userId();

        void sync();

    }

    interface View<T> {

        void updateUI();

    }
}
