package com.rotor.chappy.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.chat.ChatActivity;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
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

public class MainActivity extends AppCompatActivity implements MainInterface.View {

    private MaterialDialog materialDialog;
    private RecyclerView chatsList;
    private MainInterface.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatsList = findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatsList.setLayoutManager(mLayoutManager);

        chatsList.setAdapter(new ChatAdapter() {
            @Override
            public void onChatClicked(Chat chat) {
                presenter.goToChat(chat);
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                askForGroupName();
            }
        });


        presenter.prepareChatsFor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResumeView();
        Rotor.onResume();
        if (chatsList != null) {
            chatsList.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        Rotor.onPause();
        presenter.onPauseView();
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

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create_group) {
            askForGroupName();
            return true;
        } else if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, LoginGoogleActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
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

                            Map<String, Contact> members = new HashMap<>();
                            members.put(id, ChatManager.getContacts().getContacts().get(id));
                            Map<String, Message> messageMap = new HashMap<>();
                            Chat chat = new Chat(name.getText().toString(), members, messageMap);
                            presenter.createChat(name.getText().toString());

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
    public void openChat(Chat chat) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("path", chat.getName());
        startActivity(intent);
    }

    @Override
    public void refresh(List<Chat> chats) {
        ((ChatAdapter) chatsList.getAdapter()).chats.clear();
        ((ChatAdapter) chatsList.getAdapter()).chats.addAll(chats);
        chatsList.getAdapter().notifyDataSetChanged();
    }
}
