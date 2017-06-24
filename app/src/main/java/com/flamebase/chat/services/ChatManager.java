package com.flamebase.chat.services;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.Member;
import com.flamebase.database.FlamebaseDatabase;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 05/06/2017.
 */

public class ChatManager {

    private static final String TAG = ChatManager.class.getSimpleName();
    public static Map<String, GChat> map;
    public static Map<String, Member> contacts;
    public static RecyclerView.Adapter adapter;

    private ChatManager() {
        // nothing to do here ..
    }

    public static void init(RecyclerView.Adapter adapter) {
        ChatManager.adapter = adapter;
        if (map == null) {
            map = new HashMap<>();
        }
        if (contacts == null) {
            contacts = new HashMap<>();
        }
    }

    public static void syncGChat(final String path) {
        FlamebaseDatabase.createListener(path, new FlamebaseDatabase.FlamebaseReference<GChat>() {
            @Override
            public void onObjectChanges(GChat value) {
                if (map.containsKey(path)) {
                    map.get(path).setMember(value.getMember());
                    map.get(path).setMessages(value.getMessages());
                    map.get(path).setName(value.getName());
                } else {
                    map.put(path, value);
                }
                ChatManager.adapter.notifyDataSetChanged();
            }

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

            @Override
            public Type getType() {
                return new TypeToken<GChat>(){}.getType();
            }

        });

        LocalData.addPath(path);
    }

    public static void syncContacts(final String path) {
        FlamebaseDatabase.createListener(path, new FlamebaseDatabase.FlamebaseReference<Map<String, Member>>() {
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
                            contacts.get(entry.getKey()).setEmail(entry.getValue().getEmail());
                        }
                    }
                } else {
                    contacts = value;
                }
                ChatManager.adapter.notifyDataSetChanged();
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

            @Override
            public Type getType() {
                return new TypeToken<Map<String, Member>>(){}.getType();
            }

        });
    }

    public static Map<String, Member> getContacts() {
        return contacts;
    }

    public static Map<String, GChat> getChats() {
        return map;
    }
}
