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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {

    private MaterialDialog materialDialog;
    private RecyclerView chatsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseApp.initializeApp(this);
        LocalData.init(this);

        FlamebaseDatabase.initialize(this, getString(R.string.database_url), FirebaseInstanceId.getInstance().getToken());

        chatsList = (RecyclerView) findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatsList.setLayoutManager(mLayoutManager);
        chatsList.setAdapter(new ChatAdapter(this));

        ChatManager.init(chatsList.getAdapter());


        JSONArray array = LocalData.getLocalPaths();
        for (int i = 0; i < array.length(); i++) {
            try {
                String path = array.getString(i);
                ChatManager.syncGChat(path);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        askForEmail();

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
            askForGroupName();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void askForEmail() {
        if (isFirstRun() && materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(this)
                    .title(R.string.askIdTitle)
                    .customView(R.layout.input_identifier, true)
                    .positiveText(R.string.agree)
                    .negativeText(R.string.disagree)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            EditText name = (EditText) dialog.getCustomView().findViewById(R.id.etName);
                            EditText email = (EditText) dialog.getCustomView().findViewById(R.id.etEmail);

                            if (!TextUtils.isEmpty(name.getText()) && !TextUtils.isEmpty(email.getText())) {
                                setData(name.getText().toString(), email.getText().toString());

                                String contactPath = "/contacts";

                                ChatManager.syncContacts(contactPath);

                                Member member = new Member(name.getText().toString(), FirebaseInstanceId.getInstance().getToken(), "android", email.getText().toString());
                                ChatManager.contacts.put(email.getText().toString(), member);

                                dialog.dismiss();
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            syncUser();
        }
    }

    public void askForGroupName() {
        if (materialDialog == null) {
            materialDialog = new MaterialDialog.Builder(this)
                    .title(R.string.askIdGroupName)
                    .customView(R.layout.input_group_name, true)
                    .positiveText(R.string.agree)
                    .negativeText(R.string.disagree)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            EditText name = (EditText) dialog.getCustomView().findViewById(R.id.etName);
                            if (!TextUtils.isEmpty(name.getText())) {
                                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                                String email = prefs.getString("email", null);
                                String groupPath = "/chats/" + new Date().getTime();
                                ChatManager.syncGChat(groupPath);

                                List<String> members = new ArrayList<>();
                                members.add(email);
                                Map<String, Message> messageMap = new HashMap<>();
                                GChat gChat = new GChat(name.getText().toString(), members, messageMap);
                                ChatManager.map.put(groupPath, gChat);

                                FlamebaseDatabase.syncReference(groupPath, false);

                                dialog.dismiss();
                                materialDialog = null;
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            materialDialog = null;
                        }
                    })
                    .show();
        } else {
            syncUser();
        }
    }


    @Override
    protected void onDestroy() {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }
        super.onDestroy();
    }

    public boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString("name", null) == null || prefs.getString("email", null) == null;
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
