package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by efraespada on 11/06/2017.
 */

public class Contacts {

    @SerializedName("contacts")
    @Expose
    Map<String, User> contacts;

    public Contacts(Map<String, User> contacts) {
        this.contacts = contacts;
    }

    public Map<String, User> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, User> contacts) {
        this.contacts = contacts;
    }
}
