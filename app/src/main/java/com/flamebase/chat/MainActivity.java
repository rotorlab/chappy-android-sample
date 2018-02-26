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
import com.flamebase.chat.model.Chat;
import com.flamebase.chat.model.Member;
import com.flamebase.chat.model.Message;
import com.flamebase.chat.services.ChatManager;
import com.flamebase.chat.services.LocalData;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.interfaces.StatusListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private MaterialDialog materialDialog;
    private RecyclerView chatsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LocalData.init(this);

        chatsList = findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatsList.setLayoutManager(mLayoutManager);

        FlamebaseDatabase.initialize(this, BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void ready() {
                ChatManager.syncContacts();

                chatsList.setAdapter(new ChatAdapter(MainActivity.this));
                ChatManager.init(new ChatManager.Listener() {
                    @Override
                    public void update(List<Chat> chats) {
                        ((ChatAdapter) chatsList.getAdapter()).chats.clear();
                        ((ChatAdapter) chatsList.getAdapter()).chats.addAll(chats);
                        chatsList.getAdapter().notifyDataSetChanged();
                    }
                });


                JSONArray array = LocalData.getLocalPaths();
                for (int i = 0; i < array.length(); i++) {
                    try {
                        String path = array.getString(i);
                        ChatManager.addGChat(path);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                askForEmail();
            }

            @Override
            public void notConnected() {

            }
        });
        FlamebaseDatabase.setDebug(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FlamebaseDatabase.onResume();
    }

    @Override
    protected void onPause() {
        FlamebaseDatabase.onPause();
        super.onPause();
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
                            EditText id = (EditText) dialog.getCustomView().findViewById(R.id.etId);

                            if (!TextUtils.isEmpty(name.getText()) && !TextUtils.isEmpty(id.getText())) {
                                setUserAndSynchronize(name.getText().toString(), id.getText().toString());
                                dialog.dismiss();
                            }
                            materialDialog = null;
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
            loadUserAndSynchronize();
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
                                String id = prefs.getString(getString(R.string.var_name), null);
                                String groupPath = "/chats/" + name.getText().toString().trim().replace(" ", "_");

                                List<String> members = new ArrayList<>();
                                members.add(id);
                                Map<String, Message> messageMap = new HashMap<>();
                                Chat chat = new Chat(name.getText().toString(), members, messageMap);
                                ChatManager.map.put(groupPath, chat);
                                ChatManager.addGChat(groupPath);
                                //ChatManager.syncGChat(groupPath);

                                //FlamebaseDatabase.syncReference(groupPath, false);

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
        }
    }


    @Override
    protected void onDestroy() {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }
        //FlamebaseDatabase.removeListener("/contacts");
        super.onDestroy();
    }

    public boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString(getString(R.string.var_name), null) == null || prefs.getString(getString(R.string.var_id), null) == null;
    }

    /**
     * Loads current user as Member object and synchronizes it to server.
     */
    public void loadUserAndSynchronize() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String name = prefs.getString(getString(R.string.var_name), null);
        String id = prefs.getString(getString(R.string.var_id), null);

        Member member = new Member(name, UUID.randomUUID().toString(), getString(R.string.var_os), id);
        ChatManager.contacts.put(name, member);

        FlamebaseDatabase.syncReference(getString(R.string.contact_path), false);
    }

    /**
     * Sets current user name and ID.
     * @param name
     * @param id
     */
    public void setUserAndSynchronize(String name, String id) {
        SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
        editor.putString(getString(R.string.var_name), name).apply();
        editor.putString(getString(R.string.var_id), id).apply();

        Member member = new Member(name, FlamebaseDatabase.id, getString(R.string.var_os), id);
        ChatManager.contacts.put(name, member);

        FlamebaseDatabase.syncReference(getString(R.string.contact_path), false);
    }
}
