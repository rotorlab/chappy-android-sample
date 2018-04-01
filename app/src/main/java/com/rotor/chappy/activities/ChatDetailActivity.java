package com.rotor.chappy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rotor.chappy.R;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Contact;
import com.rotor.chappy.services.ChatManager;
import com.rotor.core.Rotor;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.model.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rotor.chappy.activities.SplashActivity.ACTION_CHAT;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView memberList;
    private Chat chat;
    private String path;
    private MaterialDialog materialDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        memberList = findViewById(R.id.member_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        memberList.setLayoutManager(linearLayoutManager);
        memberList.setAdapter(new MemberAdapter());

        Database.listen(path, new Reference<Chat>(Chat.class) {
            @Override
            public void onCreate() {
                finish();
            }

            @Override
            public void onChanged(@NonNull Chat chat) {
                ChatDetailActivity.this.chat = chat;
                ChatDetailActivity.this.setTitle(chat.getName());
                memberList.getAdapter().notifyDataSetChanged();
            }

            @Nullable
            @Override
            public Chat onUpdate() {
                return ChatDetailActivity.this.chat;
            }

            @Override
            public void onDestroy() {
                chat = null;
                finish();
            }

            @Override
            public void progress(int i) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Rotor.onResume();
    }

    @Override
    protected void onPause() {
        Rotor.onPause();
        super.onPause();
    }

    public void askForMessage(final String token) {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }

        materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.zumb_title)
                .customView(R.layout.input_group_name, true)
                .positiveText(R.string.agree)
                .negativeText(R.string.disagree)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final EditText message = dialog.getCustomView().findViewById(R.id.etName);
                        if (!TextUtils.isEmpty(message.getText())) {
                            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                            final String id = prefs.getString(getString(R.string.var_id), null);

                            Contact contact = ChatManager.getContacts().getContacts().get(id);

                            Content content = new Content(ACTION_CHAT,
                                    chat.getName(),
                                    contact.getName() + ": " + message.getText().toString(),
                                    chat.getName(),
                                    "myChannel",
                                    "Test channel",
                                    null,
                                    null);

                            ArrayList<String> ids = new ArrayList<>();
                            ids.add(token);
                            Notifications.notify(Notifications.builder(content, ids));

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


    public class MemberAdapter extends RecyclerView.Adapter<ViewHolder> {

        private MemberAdapter() {
            // nothing to do here
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            List<Contact> members = new ArrayList<>();
            for (Map.Entry<String, Contact> entry : chat.getMembers().entrySet()) {
                members.add(entry.getValue());
            }

            final Contact contact = members.get(position);
            holder.name.setText(contact.getName());
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            final String id = prefs.getString(getString(R.string.var_id), null);
            holder.dm.setVisibility(!contact.getId().equals(id) ? View.VISIBLE :  View.GONE);
            holder.dm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askForMessage(contact.getToken());
                }
            });
        }

        @Override
        public int getItemCount() {
            if (chat == null || chat.getMembers() == null) {
                return 0;
            } else {
                return chat.getMembers().size();
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout content;
        TextView name;
        Button dm;

        ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.member_content);
            name = itemView.findViewById(R.id.name);
            dm = itemView.findViewById(R.id.dm);
        }
    }

}
