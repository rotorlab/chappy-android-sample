package com.rotor.chappy.fragments.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.R;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Message;
import com.rotor.chappy.model.User;

import org.apache.commons.lang3.StringEscapeUtils;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VHMessages> {

    private ChatFragment fragment;

    public MessageAdapter(ChatFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    @NonNull
    public VHMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VHMessages(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VHMessages holder, int position) {
        Chat chat = fragment.presenter.chat();
        Message[] messages = (Message[]) chat.getMessages().values().toArray();

        Message message = messages[(messages.length - 1) - position];

        if (fragment.presenter.users().containsKey("/users/" + message.getAuthor())) {
            User user = fragment.presenter.users().get("/users/" + message.getAuthor());
            holder.author.setText(user.getName() + ":");
            holder.message.setText(StringEscapeUtils.unescapeJava(message.getText()));

            ImageLoader.getInstance().displayImage(user.getPhoto(), holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if (fragment.presenter.chat() == null || fragment.presenter.chat().getMessages() == null) {
            return 0;
        } else {
            return fragment.presenter.chat().getMessages().size();
        }
    }

    static class VHMessages extends RecyclerView.ViewHolder {

        RelativeLayout content;
        RoundedImageView image;
        TextView author;
        TextView message;

        VHMessages(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.message_content);
            image = itemView.findViewById(R.id.image);
            author = itemView.findViewById(R.id.author);
            message = itemView.findViewById(R.id.message);
        }
    }
}
