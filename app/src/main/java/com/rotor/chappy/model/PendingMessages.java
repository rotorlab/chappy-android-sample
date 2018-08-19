package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class PendingMessages {

    @SerializedName("messages")
    @Expose
    private HashMap<String, Message> messages;

    public PendingMessages() {
        // nothing to do here
    }

    public Map<String, Message> getMessages() {
        if (messages == null) {
            messages = new HashMap<>();
        }
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = (HashMap<String, Message>) messages;
    }
}
