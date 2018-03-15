package com.flamebase.chat.services;

import android.util.Log;

import com.flamebase.chat.model.Chat;
import com.flamebase.chat.model.GContacts;
import com.flamebase.chat.model.Member;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by efraespada on 05/06/2017.
 */

public class ChatManager {

    private static final String TAG = ChatManager.class.getSimpleName();
    public static final Map<String, Chat> map = new HashMap<>();
    public static GContacts contacts = null;
    public static Listener listener;

    public interface Listener {
        void update(List<Chat> chats);
    }

    public interface CreateChatListener {
        void newChat();
    }

    private ChatManager() {
        // nothing to do here ..
    }

    public static void setListener(Listener listener) {
        ChatManager.listener = listener;
    }

    public static void addGChat(final String path, final CreateChatListener createChatListener) {
        Database.listener(path, new Reference<Chat>() {

            @Override
            public Chat onUpdate() {
                if (map.containsKey(path)) {
                    return map.get(path);
                } else {
                    return null;
                }
            }

            @Override
            public void onChanged(Chat ref) {
                if (ref == null) {

                } else if (map.containsKey(path)) {
                    map.get(path).setMembers(ref.getMembers());
                    map.get(path).setMessages(ref.getMessages());
                    map.get(path).setName(ref.getName());
                } else {
                    map.put(path, ref);
                }
                Log.e(TAG, "chats: " + map.size());

                List<Chat> chats = new ArrayList<>();
                for (Map.Entry<String, Chat> entry : ChatManager.getChats().entrySet()) {
                    chats.add(entry.getValue());
                }

                if (listener != null) {
                    ChatManager.listener.update(chats);
                }
            }

            @Override
            public void onCreate() {
                createChatListener.newChat();
            }

            @Override
            public void progress(int value) {
                Log.e(TAG, "loading percent for " + path + " : " + value + " %");
            }

        });

        LocalData.addPath(path);
    }

    /**
     * creates a listener for given path
     */
    public static void syncContacts() {
        // TODO correct this
        final String path = "/contacts";
        Database.listener(path, new Reference<GContacts>() {

            @Override
            public void onCreate() {
                contacts = new GContacts(new HashMap<String, Member>());
                Database.sync(path);
            }

            @Override
            public void onChanged(GContacts gContacts) {
                ChatManager.contacts = gContacts;
            }

            @Override
            public GContacts onUpdate() {
                return contacts;
            }

            @Override
            public void progress(int value) {
                Log.e(TAG, "loading percent for " + path + " : " + value + " %");
            }

        });
    }

    public static void refreshChatsList() {
        List<Chat> chats = new ArrayList<>();
        for (Map.Entry<String, Chat> entry : ChatManager.getChats().entrySet()) {
            chats.add(entry.getValue());
        }

        if (listener != null) {
            ChatManager.listener.update(chats);
        }
    }

    public static GContacts getContacts() {
        return contacts;
    }

    public static Map<String, Chat> getChats() {
        return map;
    }
}
