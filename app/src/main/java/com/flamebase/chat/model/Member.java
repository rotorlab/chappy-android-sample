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

    @SerializedName("email")
    @Expose
    String email;

    @SerializedName("os")
    @Expose
    String os;

    public Member(String name, String token, String os, String email) {
        this.name = name;
        this.token = token;
        this.os = os;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
