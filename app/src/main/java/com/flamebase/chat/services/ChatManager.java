package com.flamebase.chat.services;

import android.content.Context;
import android.util.Log;

import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.Member;
import com.flamebase.chat.model.Message;
import com.flamebase.database.FlamebaseDatabase;

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
    private static Map<String, GChat> map;
    private static Map<String, Member> contacts;

    public interface Callback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String error);
    }

    private ChatManager() {
        // nothing to do here ..
    }

    public static void init(Context context) {
        ChatManager.context = context;
        if (map == null) {
            map = new HashMap<>();
        }
        if (contacts == null) {
            contacts = new HashMap<>();
        }
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

        map.put(path, gChat);

        FlamebaseDatabase.syncReference("draco", path);
    }

    public static void addContact(final String path, String email, String token, String os, String name) {
        FlamebaseDatabase.createListener("chats", path, new FlamebaseDatabase.FlamebaseReference<Map<String, Member>>() {
            @Override
            public void onObjectChanges(Map<String, Member> value) {
                if (contacts != null) {
                    for (Map.Entry<String, Member> entry : value.entrySet()) {
                        if (!contacts.containsKey(entry.getKey())) {
                            contacts.put(entry.getKey(), entry.getValue());
                        } else {
                            contacts.get(entry.getKey()).setName(entry.getValue().getName());
                            contacts.get(entry.getKey()).setOs(entry.getValue().getOs());
                            contacts.get(entry.getKey()).setToken(entry.getValue().getToken());
                        }
                    }
                } else {
                    contacts = value;
                }
            }

            @Override
            public Map<String, Member> update() {
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

        }, Map.class);

        Member member = new Member(name, token, os);
        contacts.put(email, member);

        FlamebaseDatabase.syncReference("draco", path);
    }

    public static Map<String, Member> getContacts() {
        return contacts;
    }

    public static Map<String, GChat> getChats() {
        return map;
    }
}
