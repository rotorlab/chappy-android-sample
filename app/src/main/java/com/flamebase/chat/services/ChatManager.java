package com.flamebase.chat.services;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.Member;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by efraespada on 05/06/2017.
 */

public class ChatManager {

    private static final String TAG = ChatManager.class.getSimpleName();
    public static final Map<String, GChat> map = new HashMap<>();
    public static final Map<String, FlamebaseDatabase> mapFDB = new HashMap<>();
    public static final Map<String, Member> contacts = new HashMap<>();
    public static RecyclerView.Adapter adapter;

    private ChatManager() {
        // nothing to do here ..
    }

    public static void init(RecyclerView.Adapter adapter) {
        ChatManager.adapter = adapter;
    }

    public static void syncGChat(final String path) {
        if (mapFDB.containsKey(path)) {
            mapFDB.get(path).sync();
        }
    }

    public static void addGChat(final String path) {
        if (!mapFDB.containsKey(path)) {
            FlamebaseDatabase fdb = FlamebaseDatabase.getInstance().createListener(path, new ObjectBlower<GChat>() {

                @Override
                public GChat updateObject() {
                    if (map.containsKey(path)) {
                        return map.get(path);
                    } else {
                        return null;
                    }
                }

                @Override
                public void onObjectChanged(GChat ref) {
                    if (ref == null) {

                    } else if (map.containsKey(path)) {
                        map.get(path).setMembers(ref.getMembers());
                        map.get(path).setMessages(ref.getMessages());
                        map.get(path).setName(ref.getName());
                    } else {
                        map.put(path, ref);
                    }
                    Log.e(TAG, "chats: " + map.size());

                    ChatManager.adapter.notifyDataSetChanged();
                }

                @Override
                public void progress(int value) {
                    Log.e(TAG, "loading percent for " + path + " : " + value + " %");
                }

            }, GChat.class);
            mapFDB.put(path, fdb);
            LocalData.addPath(path);
        }
    }

    /**
     * creates a listener for given path
     */
    public static void syncContacts() {

        final String path = "/contacts";
        FlamebaseDatabase.getInstance().createListener(path, new MapBlower<Member>() {

            @Override
            public Map<String, Member> updateMap() {
                return contacts;
            }

            @Override
            public void onMapChanged(Map<String, Member> ref) {
                Log.e(TAG, "reference: " + ref.size());

                for (Map.Entry<String, Member> entry : ref.entrySet()) {
                    if (!contacts.containsKey(entry.getKey())) {
                        contacts.put(entry.getKey(), entry.getValue());
                    } else {
                        contacts.get(entry.getKey()).setName(entry.getValue().getName());
                        contacts.get(entry.getKey()).setOs(entry.getValue().getOs());
                        contacts.get(entry.getKey()).setToken(entry.getValue().getToken());
                        contacts.get(entry.getKey()).setId(entry.getValue().getId());
                    }
                }
            }

            @Override
            public void progress(int value) {
                Log.e(TAG, "loading percent for " + path + " : " + value + " %");
            }

        }, Member.class);
    }

    public static Map<String, Member> getContacts() {
        return contacts;
    }

    public static Map<String, GChat> getChats() {
        return map;
    }
}
