package com.rotor.chappy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rotor.chappy.R;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Contact;
import com.rotor.chappy.model.Message;
import com.rotor.core.Rotor;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView memberList;
    private Chat chat;
    private Button sendButton;
    private EditText messageText;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        memberList = findViewById(R.id.member_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        memberList.setLayoutManager(linearLayoutManager);
        memberList.setAdapter(new MessageAdapter());

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

    public class MessageAdapter extends RecyclerView.Adapter<ViewHolder> {

        private MessageAdapter() {
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

            Contact contact = members.get(position);
            holder.name.setText(contact.getName());
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
        TextView rol;

        ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.message_content);
            name = itemView.findViewById(R.id.name);
            rol = itemView.findViewById(R.id.rol);
        }
    }

}
