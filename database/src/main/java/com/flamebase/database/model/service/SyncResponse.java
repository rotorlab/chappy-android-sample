package com.flamebase.database.model.service;


import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 08/07/2017.
 */

public class SyncResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private JsonObject data;

    @SerializedName("error")
    private String error;

    public SyncResponse() {
        // nothing to do here
    }

    public String getStatus() {
        return status;
    }

    public JsonObject getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
