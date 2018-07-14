package com.rotor.chappy.fragments.chats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rotor.chappy.R;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Chat;
import com.rotor.core.RFragment;

import java.util.HashMap;

public class ChatsFragment extends RFragment implements Frag, ChatsInterface.View {

    private HashMap<String, Chat> chats;
    public ChatsPresenter presenter;
    private RecyclerView list;
    private ChatAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chats = new HashMap<>();
        presenter = new ChatsPresenter(this);
        presenter.start();

        list = getActivity().findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        list.setLayoutManager(mLayoutManager);

        list.setAdapter(new ChatAdapter(this) {
            @Override
            public void onChatClicked(Chat chat) {
                presenter.goToChat(chat);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

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
    public void openChat(Chat chat) {

    }

    @Override
    public void chatCreated() {
        // should be called
    }

    @Override
    public void chatChanged(Chat chat) {
        chats.put(chat.getId(), chat);
    }

    @Override
    public Chat getChat(String id) {
        if (chats.containsKey(id)) return chats.get(id);
        return null;
    }

    @Override
    public void chatDeleted(Chat chat) {
        chats.remove(chat);
    }
}
