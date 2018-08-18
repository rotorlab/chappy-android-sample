package com.rotor.chappy.fragments.chats;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rotor.chappy.model.Chat;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by efraespada on 17/06/2017.
 */

public abstract class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageHolder> {

    private ChatsFragment fragment;

    public ChatAdapter(ChatsFragment fragment) {
        this.fragment = fragment;
    }

    public abstract void onChatClicked(Chat chat);

    @Override
    @NonNull
    public ChatAdapter.MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(com.rotor.chappy.R.layout.item_chat, parent, false);
        return new MessageHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MessageHolder holder, int position) {
        final Chat chat = (Chat) fragment.presenter().chats().values().toArray()[position];

        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChatClicked(chat);
            }
        });
        holder.name.setText(StringEscapeUtils.unescapeJava(chat.getName()));
    }

    @Override
    public int getItemCount() {
        return fragment.presenter().chats().size();
    }


    static class MessageHolder extends RecyclerView.ViewHolder {

        RelativeLayout content;
        TextView name;

        private MessageHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(com.rotor.chappy.R.id.chat_content);
            name = itemView.findViewById(com.rotor.chappy.R.id.group_name);
        }
    }
}



