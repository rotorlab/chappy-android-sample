package com.flamebase.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Chat {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("members")
    @Expose
    Map<String, Member> members;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public Chat(String name, Map<String, Member> members, Map<String, Message> messages) {
        this.name = name;
        this.members = members;
        this.messages = messages;
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }

    public Map<String, Message> getMessages() {
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
