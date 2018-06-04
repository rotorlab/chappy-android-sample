package com.rotor.notifications.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by efraespada on 19/03/2018.
 */

public class NotificationGetter {

    @SerializedName("method")
    @Expose
    String method;

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("receivers")
    @Expose
    List<String> receivers;

    public NotificationGetter() {
        // nothing to do here
    }

    public NotificationGetter(String method, String token, List<String> receivers) {
        this.token = token;
        this.method = method;
        this.receivers = receivers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receiver) {
        this.receivers = receiver;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
