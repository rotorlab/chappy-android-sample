package com.rotor.notifications.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 19/03/2018.
 */

public class Sender {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("moment")
    @Expose
    Long moment;

    public Sender() {
        // nothing to do here
    }

    public Sender(String id, Long moment) {
        this.id = id;
        this.moment = moment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getMoment() {
        return moment;
    }

    public void setMoment(Long moment) {
        this.moment = moment;
    }
}
