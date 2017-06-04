package com.flamebase.chat.services;

import android.content.Context;
import android.util.Log;

import com.flamebase.chat.R;
import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by efraespada on 05/06/2017.
 */

public class ChatManager {

    private static final String TAG = ChatManager.class.getSimpleName();
    private static Context context;

    public interface Callback {
        void onSuccess(JSONObject jsonObject);
        void onFailure(String error);
    }

    private ChatManager() {
        // nothing to do here ..
    }

    public static void init(Context context) {
        ChatManager.context = context;
        FirebaseApp.initializeApp(context);
    }

    public static void addContact(String id, String token, String os, String name) {
        try {
            JSONObject message = new JSONObject();
            JSONObject values = new JSONObject();
            values.put("method", "addContact");
            values.put("id", id);
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
}
