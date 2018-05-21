package com.rotor.chappy.activities.chat_detail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ChatManager;
import com.rotor.core.Rotor;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.model.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rotor.chappy.activities.splash.SplashActivity.ACTION_CHAT;

public class ChatDetailActivity extends AppCompatActivity implements ChatDetailInterface.View<Chat> {

    private RecyclerView memberList;
    private Chat chat;
    private String path;
    private MaterialDialog materialDialog;
    private ChatDetailInterface.Presenter<Chat> presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        presenter = new ChatDetailPresenter(this);

        path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        memberList = findViewById(R.id.member_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        memberList.setLayoutManager(linearLayoutManager);
        memberList.setAdapter(new MemberAdapter());

        presenter.prepareFor(path, Chat.class);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResumeView();
        Rotor.onResume();
        presenter.prepareFor(path, Chat.class);
    }

    @Override
    protected void onPause() {
        Rotor.onPause();
        presenter.onPauseView();
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

                            User user = chat.getMembers().get(id);
                            Content content = new Content(ACTION_CHAT,
                                    chat.getName(),
                                    user.getName() + ": " + message.getText().toString(),
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

    @Override
    public void onCreateReference() {
        finish();
    }

    @Override
    public void onReferenceChanged(Chat chat) {
        ChatDetailActivity.this.chat = chat;
        ChatDetailActivity.this.setTitle(chat.getName());
        memberList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public Chat onUpdateReference() {
        return ChatDetailActivity.this.chat;
    }

    @Override
    public void onDestroyReference() {
        chat = null;
        finish();
    }

    @Override
    public void progress(int value) {

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
            List<User> members = new ArrayList<>();
            for (Map.Entry<String, User> entry : chat.getMembers().entrySet()) {
                members.add(entry.getValue());
            }

            final User user = members.get(position);
            holder.name.setText(user.getName());
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            final String id = prefs.getString(getString(R.string.var_id), null);
            holder.dm.setVisibility(!user.getUid().equals(id) ? View.VISIBLE :  View.GONE);
            holder.dm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askForMessage(user.getToken());
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
