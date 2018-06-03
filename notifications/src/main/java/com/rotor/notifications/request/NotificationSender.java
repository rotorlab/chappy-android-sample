package com.rotor.notifications.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rotor.notifications.model.Receiver;

import java.util.List;

/**
 * Created by efraespada on 19/03/2018.
 */

public class NotificationSender {

    @SerializedName("method")
    @Expose
    String method;

    @SerializedName("notificationId")
    @Expose
    String id;

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("receivers")
    @Expose
    List<Receiver> receivers;

    public NotificationSender() {
        // nothing to do here
    }

    public NotificationSender(String method, String token, String id, List<Receiver> receivers) {
        this.token = token;
        this.method = method;
        this.id = id;
        this.receivers = receivers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNotificationId() {
        return id;
    }

    public void setNotificationId(String id) {
        this.id = id;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receiver) {
        this.receivers = receiver;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
