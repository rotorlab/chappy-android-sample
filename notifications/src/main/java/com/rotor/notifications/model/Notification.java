package com.rotor.notifications.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by efraespada on 19/03/2018.
 */

public class Notification {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("time")
    @Expose
    Long time;

    @SerializedName("content")
    @Expose
    Content content;

    @SerializedName("sender")
    @Expose
    Sender sender;

    @SerializedName("receivers")
    @Expose
    HashMap<String, Receiver> receivers;

    public Notification() {
        // nothing to do here
    }

    public Notification(String id, Long time, Content content, Sender sender, HashMap<String, Receiver> receivers) {
        this.id = id;
        this.time = time;
        this.content = content;
        this.sender = sender;
        this.receivers = receivers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public HashMap<String, Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(HashMap<String, Receiver> receivers) {
        this.receivers = receivers;
    }
}
