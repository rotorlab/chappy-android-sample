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
    String notificationId;

    @SerializedName("receivers")
    @Expose
    List<Receiver> receivers;

    public NotificationSender() {
        // nothing to do here
    }

    public NotificationSender(String method, String notificationId, List<Receiver> receivers) {
        this.method = method;
        this.notificationId = notificationId;
        this.receivers = receivers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receiver) {
        this.receivers = receiver;
    }
}
