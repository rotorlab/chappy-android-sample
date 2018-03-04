package com.flamebase.chat.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Member {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("os")
    @Expose
    String os;

    public Member(String name, String token, String os, String id) {
        this.name = name;
        this.token = token;
        this.os = os;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
