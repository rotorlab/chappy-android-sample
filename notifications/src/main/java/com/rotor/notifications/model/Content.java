package com.rotor.notifications.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 19/03/2018.
 */

public class Content {

    @SerializedName("title")
    @Expose
    String title;

    @SerializedName("body")
    @Expose
    String body;

    @SerializedName("channel")
    @Expose
    String channel;

    @SerializedName("channelDescription")
    @Expose
    String channelDescription;

    @SerializedName("photoSmall")
    @Expose
    String photoSmall;

    @SerializedName("photo")
    @Expose
    String photo;

    public Content() {
        // nothing to do here
    }

    public Content(String title, String body, String channel, String channelDescription, String photoSmall, String photo) {
        this.title = title;
        this.body = body;
        this.channel = channel;
        this.channelDescription = channelDescription;
        this.photoSmall = photoSmall;
        this.photo = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelDescription() {
        return channelDescription;
    }

    public void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
    }

    public String getPhotoSmall() {
        return photoSmall;
    }

    public void setPhotoSmall(String photoSmall) {
        this.photoSmall = photoSmall;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
