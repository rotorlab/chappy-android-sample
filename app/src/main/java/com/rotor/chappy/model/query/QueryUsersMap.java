package com.rotor.chappy.model.query;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 06/08/2018.
 */

public class QueryUsersMap {

    @SerializedName("members")
    @Expose
    private Map<String, Member> members;

    public QueryUsersMap(String id) {
        this.members = new HashMap<>();
        members.put("*", new Member(id));
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public static class Member {

        @SerializedName("id")
        @Expose
        private String userId;

        public Member() {
            this.userId = "";
        }

        public Member(String id) {
            this.userId = id;
        }

        public String getUserId() {
            return userId;
        }

    }

}
