package com.rotor.chappy.activities;

import android.content.Context;
import android.content.Intent;
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
import com.rotor.chappy.R;
import com.rotor.chappy.adapters.ChatAdapter;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Contact;
import com.rotor.chappy.model.Message;
import com.rotor.chappy.services.ChatManager;
import com.rotor.core.Rotor;
import com.rotor.database.Database;

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

        chatsList = findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatsList.setLayoutManager(mLayoutManager);

        chatsList.setAdapter(new ChatAdapter() {
            @Override
            public void onChatClicked(Chat chat) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("path", chat.getName());
                MainActivity.this.startActivity(intent);
            }
        });

        ChatManager.setListener(new ChatManager.Listener() {
            @Override
            public void update(List<Chat> chats) {
                ((ChatAdapter) chatsList.getAdapter()).chats.clear();
                ((ChatAdapter) chatsList.getAdapter()).chats.addAll(chats);
                chatsList.getAdapter().notifyDataSetChanged();
            }
        });

        ChatManager.refreshChatsList();

        askForEmail();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                askForGroupName();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Rotor.onResume();
        if (chatsList != null) {
            chatsList.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        Rotor.onPause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                    .title(com.rotor.chappy.R.string.askIdTitle)
                    .customView(R.layout.input_identifier, true)
                    .positiveText(com.rotor.chappy.R.string.agree)
                    .negativeText(com.rotor.chappy.R.string.disagree)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            EditText name = dialog.getCustomView().findViewById(R.id.etName);
                            if (!TextUtils.isEmpty(name.getText())) {
                                setUserAndSynchronize(name.getText().toString());
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
        }
    }

    public void askForGroupName() {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }

        materialDialog = new MaterialDialog.Builder(this)
                .title(com.rotor.chappy.R.string.askIdGroupName)
                .customView(R.layout.input_group_name, true)
                .positiveText(com.rotor.chappy.R.string.agree)
                .negativeText(com.rotor.chappy.R.string.disagree)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final EditText name = dialog.getCustomView().findViewById(R.id.etName);
                        if (!TextUtils.isEmpty(name.getText())) {
                            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                            final String id = prefs.getString(getString(R.string.var_id), null);
                            final String groupPath = "/chats/" + name.getText().toString().trim().replace(" ", "_");

                            ChatManager.addGChat(groupPath, new ChatManager.CreateChatListener() {
                                @Override
                                public void newChat() {
                                    Map<String, Contact> members = new HashMap<>();
                                    members.put(id, ChatManager.getContacts().getContacts().get(id));
                                    Map<String, Message> messageMap = new HashMap<>();
                                    Chat chat = new Chat(name.getText().toString(), members, messageMap);
                                    ChatManager.map.put(groupPath, chat);
                                    Database.sync(groupPath);
                                }
                            });

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
        String id = prefs.getString(getString(com.rotor.chappy.R.string.var_id), null);
        return id == null || !ChatManager.contacts.getContacts().containsKey(id);
    }

    /**
     * Sets current user name and ID.
     * @param name
     */
    public void setUserAndSynchronize(String name) {
        SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
        editor.putString(getString(com.rotor.chappy.R.string.var_name), name).apply();

        String id = UUID.randomUUID().toString();

        editor.putString(getString(com.rotor.chappy.R.string.var_id), id).apply();

        Contact contact = new Contact(name, Rotor.getId(), getString(com.rotor.chappy.R.string.var_os), id);
        ChatManager.contacts.getContacts().put(id, contact);

        Database.sync(getString(com.rotor.chappy.R.string.contact_path), false);
    }
}
