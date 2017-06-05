package com.flamebase.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class GChat {

    @SerializedName("people")
    @Expose
    Map<String, Person> people;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public GChat(Map<String, Person> people, Map<String, Message> messages) {
        this.people = people;
        this.messages = messages;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = messages;
    }

}
