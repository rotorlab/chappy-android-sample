package com.flamebase.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by efraespada on 04/06/2017.
 */

public class Message {

    @SerializedName("author")
    @Expose
    String author;

    @SerializedName("text")
    @Expose
    String text;

    public Message(String author, String text) {
        this.author = author;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
