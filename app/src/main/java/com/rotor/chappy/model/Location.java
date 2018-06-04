package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("latitude")
    @Expose
    Double latitude;

    @SerializedName("longitude")
    @Expose
    Double longitude;

    @SerializedName("altitude")
    @Expose
    Double altitude;

    @SerializedName("speed")
    @Expose
    Float speed;

    @SerializedName("accuracy")
    @Expose
    Float accuracy;

    @SerializedName("steps")
    @Expose
    Long steps;

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("type")
    @Expose
    String type;

    @SerializedName("id")
    @Expose
    String id;

    public Location() {
        // nothing to do here
    }

    public Location(Double latitude, Double longitude, Double altitude, Float speed, Float accuracy, Long steps, String name, String type, String id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.accuracy = accuracy;
        this.steps = steps;
        this.name = name;
        this.type = type;
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Long getSteps() {
        return steps;
    }

    public void setSteps(Long steps) {
        this.steps = steps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
