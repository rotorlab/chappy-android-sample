package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by efraespada on 04/06/2017.
 */

public class ResponseId {

    @SerializedName("result")
    @Expose
    private List<Id> id;


    public ResponseId() {
        // nothing to do here
    }

    public List<Id> getIds() {
        return id;
    }

    public static class Id {

        @SerializedName("id")
        @Expose
        private String userId;

        public Id() {
            this.userId = "";
        }

        public String getUserId() {
            return userId;
        }

    }

}
