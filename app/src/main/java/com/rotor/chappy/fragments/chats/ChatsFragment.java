package com.rotor.chappy.fragments.chats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rotor.chappy.R;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.core.RFragment;

public class ChatsFragment extends RFragment implements Frag<ChatsFragment> {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    @Override
    public ChatsFragment instance() {
        return new ChatsFragment();
    }
}
