package com.rotor.chappy.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 04/06/2017.
 */

public class ResponseUsersMap {

    @SerializedName("result")
    @Expose
    private List<UsersMapChat> chats;


    public ResponseUsersMap() {
        // nothing to do here
    }

    public List<UsersMapChat> getChats() {
        return chats;
    }

    public static class UsersMapChat {

        @SerializedName("id")
        @Expose
        private String userId;

        @SerializedName("members")
        @Expose
        private Map<String, Member> members;

        public UsersMapChat() {
            this.userId = "";
        }

        public String getUserId() {
            return userId;
        }

        public Map<String, Member> getMembers() {
            return members;
        }

        public static class Member {

            @SerializedName("id")
            @Expose
            private String userId;

            @SerializedName("rol")
            @Expose
            private String rol;

            public Member() {
                this.userId = "";
                this.rol = "";
            }

            public String getUserId() {
                return userId;
            }

            public String getRol() {
                return rol;
            }

        }

    }

}
