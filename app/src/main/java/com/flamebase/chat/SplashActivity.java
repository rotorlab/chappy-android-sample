package com.flamebase.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.flamebase.chat.services.ChatManager;
import com.flamebase.chat.services.LocalData;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.StatusListener;
import com.rotor.database.Database;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by efraespada on 27/02/2018.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalData.init(getApplicationContext());

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void connected() {
                Database.initialize();
                ChatManager.syncContacts();

                JSONArray array = LocalData.getLocalPaths();
                for (int i = 0; i < array.length(); i++) {
                    try {
                        final String path = array.getString(i);
                        ChatManager.addGChat(path, new ChatManager.CreateChatListener() {
                            @Override
                            public void newChat() {
                                Database.removeListener(path);
                                LocalData.removePath(path);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }

            @Override
            public void reconnecting() {

            }
        });
        Rotor.debug(true);
    }
}
