package com.rotor.chappy.fragments.chats;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.home.HomeActivity;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.Message;
import com.rotor.core.RFragment;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Date;
import java.util.HashMap;

public class ChatsFragment extends RFragment implements Frag, ChatsInterface.View {

    private Toolbar toolbar;
    private ChatsPresenter presenter;
    private ChatAdapter adapter;
    private MaterialDialog materialDialog;

    @Nullable
    @Override
    public View onCreateRView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onRViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new ChatsPresenter(this);

        toolbar = view.findViewById(R.id.toolbar);

        RecyclerView list = view.findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        list.setLayoutManager(mLayoutManager);

        adapter = new ChatAdapter(this) {
            @Override
            public void onChatClicked(Chat chat) {
                presenter.goToChat(chat);
            }
        };

        list.setAdapter(adapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResumeView() {
        presenter.start();
        ((HomeActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("Chats");
    }

    @Override
    public void onPauseView() {
        // nothing to do here
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }

    @Override
    public void connected() {
        // nothing to do here
    }

    @Override
    public void disconnected() {
        // nothing to do here
    }

    @Override
    public FragmentType type() {
        return FragmentType.CHATS;
    }

    @Override
    public String title() {
        return "Chats";
    }

    public static ChatsFragment instance() {
        return new ChatsFragment();
    }

    @Override
    public void chatChanged(Chat chat) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create_group) {
            askGroupName();
            return true;
        } else if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(getActivity())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getActivity(), LoginGoogleActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void askGroupName() {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }

        materialDialog = new MaterialDialog.Builder(getActivity())
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
                            long creation = new Date().getTime();
                            Chat chat = new Chat();
                            chat.setCreation(creation);
                            chat.setId(String.valueOf(creation));
                            chat.setName(StringEscapeUtils.escapeJava(name.getText().toString()));
                            chat.setMembers(new HashMap<String, Member>());
                            chat.setMessages(new HashMap<String, Message>());
                            Member member = new Member();
                            member.setDate(new Date().getTime());
                            member.setId(FirebaseAuth.getInstance().getUid());
                            member.setRol("admin");
                            chat.addMember(member);
                            presenter.createChat(chat);
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
    public ChatsPresenter presenter() {
        return presenter;
    }
}
