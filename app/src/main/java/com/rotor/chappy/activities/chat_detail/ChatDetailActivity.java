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
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.chat.ChatActivity;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.Message;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.core.Rotor;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.model.Content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.rotor.chappy.activities.splash.SplashActivity.ACTION_CHAT;

public class ChatDetailActivity extends AppCompatActivity implements ChatDetailInterface.View<Chat>, ProfilesView {

    private RecyclerView memberList;
    private Chat chat;
    private String path;
    private MaterialDialog materialDialog;
    private ChatDetailPresenter presenter;
    private static final Map<String, User> users = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        presenter = new ChatDetailPresenter(this, this);

        path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        memberList = findViewById(R.id.member_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        memberList.setLayoutManager(linearLayoutManager);
        memberList.setAdapter(new MemberAdapter());
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

                            Member member = chat.getMembers().get(id);
                            if (users.containsKey(member.getId())) {
                                User user = users.get(member.getId());
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
                            }

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

        for (Map.Entry<String, Member> entry : chat.getMembers().entrySet()) {
            if (!users.containsKey("/users/" + entry.getValue().getId())) {
                presenter.prepareProfileFor("/users/" + entry.getValue().getId());
            }
        }

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

    @Override
    public void onCreateUser(String key) {
        // should not be called
    }

    @Override
    public void onUserChanged(String key, User user) {
        users.put(key, user);
        memberList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public User onUpdateUser(String key) {
        return users.get(key);
    }

    @Override
    public void onDestroyUser(String key) {
        users.remove(key);
        memberList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void userProgress(String key, int value) {

    }


    public class MemberAdapter extends RecyclerView.Adapter<VHMember> {

        private MemberAdapter() {
            // nothing to do here
        }

        @Override
        @NonNull
        public VHMember onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new VHMember(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull VHMember holder, int position) {
            List<Member> members = new ArrayList<>();
            for (Map.Entry<String, Member> entry : chat.getMembers().entrySet()) {
                members.add(entry.getValue());
            }
            final Member member = members.get(position);

            if (users.containsKey(member.getId())) {
                final User user = users.get(member.getId());
                holder.name.setText(user.getName());
                ImageLoader.getInstance().displayImage(user.getPhoto(), holder.image);

                holder.dm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        askForMessage(user.getToken());
                    }
                });
            }
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

    static class VHMember extends RecyclerView.ViewHolder {

        RelativeLayout content;
        RoundedImageView image;
        TextView name;
        Button dm;

        VHMember(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.member_content);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            dm = itemView.findViewById(R.id.dm);
        }
    }

}
