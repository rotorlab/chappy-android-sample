package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Chat {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("contacts")
    @Expose
    Map<String, Contact> members;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public Chat(String name, Map<String, Contact> members, Map<String, Message> messages) {
        this.name = name;
        this.members = members;
        this.messages = messages;
    }

    public Map<String, Contact> getMembers() {
        if (members == null) {
            members = new HashMap<>();
        }
        return members;
    }

    public void setMembers(Map<String, Contact> members) {
        this.members = members;
    }

    public Map<String, Message> getMessages() {
        if (messages == null) {
            messages = new HashMap<>();
        }
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
