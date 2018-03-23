package com.rotor.chappy.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.rotor.chappy.ContactsListener;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Contact;
import com.rotor.chappy.model.Contacts;
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
    public static Contacts contacts = null;
    public static Listener listener;
    public static boolean shouldStart = false;

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
        Database.listen(path, new Reference<Chat>(Chat.class) {

            @Override
            public void onCreate() {
                createChatListener.newChat();
            }

            @Override
            public Chat onUpdate() {
                if (map.containsKey(path)) {
                    return map.get(path);
                } else {
                    return null;
                }
            }

            @Override
            public void onChanged(@NonNull Chat ref) {
                if (map.containsKey(path)) {
                    map.get(path).setMembers(ref.getMembers());
                    map.get(path).setMessages(ref.getMessages());
                    map.get(path).setName(ref.getName());
                } else {
                    map.put(path, ref);
                }
                updateList();
            }

            @Override
            public void onDestroy() {
                if (map.containsKey(path)) {
                    map.remove(path);
                }
                updateList();
            }

            @Override
            public void progress(int value) {
                Log.e(TAG, "loading percent for " + path + " : " + value + " %");
            }

        });

        LocalData.addPath(path);
    }

    private static void updateList() {
        Log.e(TAG, "chats: " + map.size());

        List<Chat> chats = new ArrayList<>();
        for (Map.Entry<String, Chat> entry : ChatManager.getChats().entrySet()) {
            chats.add(entry.getValue());
        }

        if (listener != null) {
            ChatManager.listener.update(chats);
        }
    }

    /**
     * creates a listener for given path
     */
    public static void splashSyncContacts(final ContactsListener contactsListener) {
        final String path = "/contacts";
        shouldStart = true;
        Database.listen(path, new Reference<Contacts>(Contacts.class) {

            @Override
            public void onDestroy() {
                contacts = null;
            }

            @Override
            public void onCreate() {
                contacts = new Contacts(new HashMap<String, Contact>());
                Database.sync(path);
            }

            @Override
            public void onChanged(Contacts contacts) {
                ChatManager.contacts = contacts;
                if (shouldStart) {
                    shouldStart = false;
                    contactsListener.contactsReady();
                }
            }

            @Override
            public Contacts onUpdate() {
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

    public static Contacts getContacts() {
        return contacts;
    }

    public static Map<String, Chat> getChats() {
        return map;
    }
}
