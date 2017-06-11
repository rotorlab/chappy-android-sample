package com.flamebase.chat.services;

import android.content.Context;
import android.util.Log;

import com.flamebase.chat.R;
import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.GContacts;
import com.flamebase.chat.model.Member;
import com.flamebase.chat.model.Message;
import com.flamebase.database.FlamebaseDatabase;
import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 05/06/2017.
 */

public class ChatManager {

    private static final String TAG = ChatManager.class.getSimpleName();
    private static Context context;
    private static HashMap<String, GChat> map;
    private static GContacts contacts;

    public interface Callback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String error);
    }

    private ChatManager() {
        // nothing to do here ..
    }

    public static void init(Context context) {
        ChatManager.context = context;
        map = new HashMap<>();
        contacts = null;

    }

    public static void syncGChat(final String path, String email, String name) {
        FlamebaseDatabase.createListener("chats", path, new FlamebaseDatabase.FlamebaseReference<GChat>() {
            @Override
            public void onObjectChanges(GChat value) {
                if (map.containsKey(path)) {
                    map.get(path).setMember(value.getMember());
                    map.get(path).setMessages(value.getMessages());
                    map.get(path).setName(value.getName());
                } else {
                    map.put(path, value);
                }
            }

            // TODO make it work ------*
            @Override
            public GChat update() {
                if (map.containsKey(path)) {
                    return map.get(path);
                } else {
                    return null;
                }
            }

            @Override
            public void progress(String id, int value) {
                Log.e(TAG, "loading percent for " + id + " : " + value + " %");
            }

            @Override
            public String getTag() {
                return path + "_sync";
            }

        }, GChat.class);


        List<String> members = new ArrayList<>();
        members.add(email);
        Map<String, Message> messageMap = new HashMap<>();
        GChat gChat = new GChat(name, members, messageMap);

        FlamebaseDatabase.syncReference(path, gChat);
    }

    public static void addContact(final String path, String email, String token, String os, String name) {
        FlamebaseDatabase.createListener("chats", path, new FlamebaseDatabase.FlamebaseReference<GContacts>() {
            @Override
            public void onObjectChanges(GContacts value) {
                if (contacts != null) {
                    contacts.setMembers(value.getMembers());
                } else {
                    contacts = value;
                }
            }

            // TODO make it work ------*
            @Override
            public GContacts update() {
                return contacts;
            }

            @Override
            public void progress(String id, int value) {
                Log.e(TAG, "loading percent for " + id + " : " + value + " %");
            }

            @Override
            public String getTag() {
                return path + "_sync";
            }

        }, GContacts.class);


        Map<String, Member> memberMap = new HashMap<>();
        Member member = new Member(name, token, os);
        memberMap.put(email, member);
        GContacts contacts = new GContacts(memberMap);

        FlamebaseDatabase.syncReference(path, contacts);
    }

    /*

    public static void addContact(String email, String token, String os, String name) {
        try {
            JSONObject message = new JSONObject();
            JSONObject values = new JSONObject();
            values.put("method", "addContact");
            values.put("email", email);
            values.put("token", token);
            values.put("name", name);
            values.put("os", os);
            message.put("message", values);
            Sender.postRequest(ChatManager.context.getString(R.string.server_url), message.toString(), new Sender.Callback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    Log.e(TAG, "action success");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "action with error: " + error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


 */
    public static void createGroup(String name, String email) {
        try {
            JSONObject message = new JSONObject();
            JSONObject values = new JSONObject();
            values.put("method", "createGroup");
            values.put("email", email);
            values.put("name", name);
            message.put("message", values);
            Sender.postRequest(ChatManager.context.getString(R.string.server_url), message.toString(), new Sender.Callback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    Log.e(TAG, "action success");
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "action with error: " + error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
