package com.rotor.notifications.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 19/03/2018.
 */

public class Receiver {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("viewed")
    @Expose
    Long viewed;

    public Receiver() {
        // nothing to do here
    }

    public Receiver(String id, Long viewed) {
        this.id = id;
        this.viewed = viewed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getViewed() {
        return viewed;
    }

    public void setViewed(Long viewed) {
        this.viewed = viewed;
    }
}
