package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Member {

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("rol")
    @Expose
    String rol;

    @SerializedName("date")
    @Expose
    long date;

    public Member() {
        // nothing to do here
    }

    public Member(String rol, long date) {
        this.rol = rol;
        this.date = date;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
