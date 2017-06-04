package com.flamebase.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Person {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("os")
    @Expose
    String os;

    public Person(String name, String token, String os) {
        this.name = name;
        this.token = token;
        this.os = os;
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
}
