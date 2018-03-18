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
    Map<String, Contact> contacts;

    public Contacts(Map<String, Contact> contacts) {
        this.contacts = contacts;
    }

    public Map<String, Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, Contact> contacts) {
        this.contacts = contacts;
    }
}
