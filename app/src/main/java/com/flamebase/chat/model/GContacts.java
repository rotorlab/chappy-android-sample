package com.flamebase.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by efraespada on 11/06/2017.
 */

public class GContacts {

    @SerializedName("contacts")
    @Expose
    Map<String, Member> members;

    public GContacts(Map<String, Member> members) {
        this.members = members;
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }
}
