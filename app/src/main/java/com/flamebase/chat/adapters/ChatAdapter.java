package com.flamebase.chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flamebase.chat.ChatActivity;
import com.flamebase.chat.R;
import com.flamebase.chat.model.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by efraespada on 17/06/2017.
 */

public abstract class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final List<Chat> chats = new ArrayList<>();

    public ChatAdapter() {
        // nothing to do here
    }

    public abstract void onChatClicked(Chat chat);

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        final Chat chat = chats.get(position);

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChatClicked(chat);
            }
        });
        holder.name.setText(chat.getName());
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout content;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.chat_content);
            name = itemView.findViewById(R.id.group_name);
        }
    }
}



