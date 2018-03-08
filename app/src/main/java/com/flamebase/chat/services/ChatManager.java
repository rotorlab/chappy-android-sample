package com.flamebase.chat.services;

import android.util.Log;

import com.flamebase.chat.model.Chat;
import com.flamebase.chat.model.Member;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.interfaces.MapBlower;
import com.flamebase.database.interfaces.ObjectBlower;

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
    public static final Map<String, Member> contacts = new HashMap<>();
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
        FlamebaseDatabase.listener(path, new ObjectBlower<Chat>() {

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

        }, Chat.class);

        LocalData.addPath(path);
    }

    /**
     * creates a listener for given path
     */
    public static void syncContacts() {
        // TODO correct this
        final String path = "/contacts";
        FlamebaseDatabase.listener(path, new MapBlower<Member>() {

            @Override
            public Map<String, Member> onUpdate() {
                return contacts;
            }

            @Override
            public void onChanged(Map<String, Member> ref) {
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
            public void onCreate() {
                FlamebaseDatabase.sync(path);
            }

            @Override
            public void progress(int value) {
                Log.e(TAG, "loading percent for " + path + " : " + value + " %");
            }

        }, Member.class);
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

    public static Map<String, Member> getContacts() {
        return contacts;
    }

    public static Map<String, Chat> getChats() {
        return map;
    }
}
