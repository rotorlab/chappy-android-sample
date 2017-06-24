package com.flamebase.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flamebase.chat.adapters.ChatAdapter;
import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.Member;
import com.flamebase.chat.model.Message;
import com.flamebase.chat.services.ChatManager;
import com.flamebase.chat.services.LocalData;
import com.flamebase.database.FlamebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseApp.initializeApp(this);
        LocalData.init(this);

        messageList = (RecyclerView) findViewById(R.id.messages_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        messageList.setLayoutManager(mLayoutManager);
        messageList.setAdapter(new ChatAdapter(this));

        ChatManager.init(messageList.getAdapter());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create_group) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void syncUser() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String name = prefs.getString("name", null);
        String email = prefs.getString("email", null);
        String contactPath = "/contacts";
        ChatManager.syncContacts(contactPath);

        Member member = new Member(name, FirebaseInstanceId.getInstance().getToken(), "android", email);
        ChatManager.contacts.put(name, member);

        FlamebaseDatabase.syncReference(contactPath, false);
    }


    public void setData(String name, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
        editor.putString("name", name).apply();
        editor.putString("email", email).apply();
        String contactPath = "/contacts";

        ChatManager.syncContacts(contactPath);

        Member member = new Member(name, FirebaseInstanceId.getInstance().getToken(), "android", email);
        ChatManager.contacts.put(name, member);

        FlamebaseDatabase.syncReference(contactPath, false);
    }
}
