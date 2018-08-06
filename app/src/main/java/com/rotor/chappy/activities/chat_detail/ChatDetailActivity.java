package com.rotor.chappy.activities.chat_detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.rotor.chappy.activities.contact_scanner.ContactScannerActivity;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.core.RAppCompatActivity;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.model.Content;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rotor.chappy.activities.splash.SplashActivity.ACTION_CHAT;

public class ChatDetailActivity extends RAppCompatActivity implements ChatDetailInterface.View {

    private RecyclerView memberList;
    private String uidToAdd;
    private MaterialDialog materialDialog;
    private ChatDetailPresenter presenter;
    public static final int SCANNER_CODE = 2345;

    public List<String> loaded = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new ChatDetailPresenter(this);

        memberList = findViewById(R.id.member_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        memberList.setLayoutManager(linearLayoutManager);
        memberList.setAdapter(new MemberAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.start(getIntent());
        if (uidToAdd != null) {
            addUid(uidToAdd);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_contact) {
            Intent intent = new Intent(this, ContactScannerActivity.class);
            startActivityForResult(intent, SCANNER_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SCANNER_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    uidToAdd = res.getString("uid");
                }
                break;
        }
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
                        if (!TextUtils.isEmpty(message.getText()))
                            if (presenter.members().containsKey(presenter.userId())) {
                                User user = presenter.users().get(presenter.userId());
                                Content content = new Content(ACTION_CHAT,
                                        presenter.chat().getName(),
                                        user.getName() + ": " + message.getText().toString(),
                                        presenter.chat().getId(),
                                        "myChannel",
                                        "Test channel",
                                        user.getPhoto(),
                                        null);

                                ArrayList<String> ids = new ArrayList<>();
                                ids.add(token);
                                Notifications.notify(Notifications.builder(content, ids));
                            }

                        dialog.dismiss();
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

    public void addUid(String uid) {
        Member member = new Member();
        member.setId(uid);
        member.setRol("basic");
        member.setDate(new Date().getTime());
        presenter.chat().addMember(member);
        presenter.sync();
        Log.e("UID", "result:" + uid);

        if (presenter.users().containsKey(presenter.userId())) {
            User user = presenter.users().get(presenter.userId());
            Content content = new Content(ACTION_CHAT,
                    getString(R.string.notification_message_added_to_group_title),
                    getString(R.string.notification_message_added_to_group, presenter.chat().getName()),
                    presenter.chat().getId(),
                    "myChannel",
                    "Test channel",
                    user.getPhoto(),
                    null);

            ArrayList<String> ids = new ArrayList<>();
            ids.add(uid);
            Notifications.notify(Notifications.builder(content, ids));
        }
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

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
            for (Map.Entry<String, Member> entry : presenter.chat().getMembers().entrySet()) {
                members.add(entry.getValue());
            }
            final Member member = members.get(position);

            if (presenter.users().containsKey(member.getId())) {
                final User user = presenter.users().get(member.getId());
                holder.name.setText(user.getName());
                ImageLoader.getInstance().displayImage(user.getPhoto(), holder.image);
                if (user.getUid().equals(presenter.userId())) {
                    holder.name.setText(getString(R.string.name_me));
                    holder.dm.setVisibility(View.GONE);
                    holder.dm.setOnClickListener(null);
                } else {
                    holder.name.setText(user.getName());
                    holder.dm.setVisibility(View.VISIBLE);
                    holder.dm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            askForMessage(user.getToken());
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            if (presenter.chat() == null || presenter.chat().getMembers() == null) {
                return 0;
            } else {
                return presenter.chat().getMembers().size();
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

    @Override
    public void updateUI() {
        setTitle(presenter.chat().getName());
        memberList.getAdapter().notifyDataSetChanged();
    }
}
