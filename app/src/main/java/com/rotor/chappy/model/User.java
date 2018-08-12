package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class User {

    @SerializedName("uid")
    @Expose
    String uid;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("email")
    @Expose
    String email;

    @SerializedName("photo")
    @Expose
    String photo;

    @SerializedName("os")
    @Expose
    String os;

    @SerializedName("token")
    @Expose
    String token;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("steps")
    @Expose
    long steps;

    @SerializedName("locations")
    @Expose
    Map<String, Location> locations;

    public User() {

    }

    public User(String uid, String name, String email, String photo, String os, String token, String type, Long steps, Map<String, Location> locations) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.os = os;
        this.token = token;
        this.type = type;
        this.steps = steps;
        this.locations = locations;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getSteps() {
        return (int) steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public Map<String, Location> getLocations() {
        return locations;
    }

    public void setLocations(Map<String, Location> locations) {
        this.locations = locations;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLastLocation() {
        Location location = null;
        if (locations != null) {
            long last = 0;
            for (Map.Entry<String, Location> entry : locations.entrySet()) {
                long time = Long.parseLong(entry.getKey());
                if (time > last) {
                    last = time;
                    location = entry.getValue();
                }
            }
        }
        return location;
    }

}
