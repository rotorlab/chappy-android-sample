package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Chat {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("creation")
    @Expose
    Long creation;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("members")
    @Expose
    Map<String, Member> members;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;



    public Chat() {
        // nothing to do here
    }

    public Chat(String name) {
        Date date = new Date();
        id = Long.valueOf(date.getTime()).toString();
        creation = date.getTime();
        this.name = name;
        members = new HashMap<>();
        messages = new HashMap<>();
    }

    public Chat(String id, Long creation, String name, Map<String, Member> members, Map<String, Message> messages) {
        this.id = id;
        this.creation = creation;
        this.name = name;
        this.members = members;
        this.messages = messages;
    }

    public Map<String, Member> getMembers() {
        if (members == null) {
            members = new HashMap<>();
        }
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }

    public void addMember(Member member) {
        this.members.put(member.getId(), member);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreation() {
        return creation;
    }

    public void setCreation(Long creation) {
        this.creation = creation;
    }
}
