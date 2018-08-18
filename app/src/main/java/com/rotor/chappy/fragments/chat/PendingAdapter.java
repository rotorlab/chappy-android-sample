package com.rotor.chappy.fragments.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rotor.chappy.R;
import com.rotor.chappy.model.Message;

import org.apache.commons.lang3.StringEscapeUtils;


public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.VHPendingMessages> {

    private ChatFragment fragment;

    public PendingAdapter(ChatFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    @NonNull
    public VHPendingMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_message, parent, false);
        return new VHPendingMessages(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VHPendingMessages holder, int position) {
        Message message = fragment.getPendingMessages().get(position);
        holder.message.setText(StringEscapeUtils.unescapeJava(message.getText()));
    }

    @Override
    public int getItemCount() {
        if (fragment.getPendingMessages() == null) {
            return 0;
        } else {
            return fragment.getPendingMessages().size();
        }
    }

    static class VHPendingMessages extends RecyclerView.ViewHolder {

        RelativeLayout content;
        TextView message;

        VHPendingMessages(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.message_content);
            message = itemView.findViewById(R.id.message);
        }
    }
}
