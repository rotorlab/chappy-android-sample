package com.rotor.chappy.enums;

import com.rotor.chappy.fragments.ChatFragment;
import com.rotor.chappy.fragments.ListFragment;

/**
 * Created by efraespada on 22/03/2018.
 */

public enum Fragments {
    LIST(ListFragment.class.getSimpleName()),
    CHAT(ChatFragment.class.getSimpleName());

    String name;

    Fragments(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
