package com.rotor.chappy.fragments.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rotor.chappy.App;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.chat_detail.ChatDetailActivity;
import com.rotor.chappy.activities.home.HomeActivity;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.fragments.chats.ChatsFragment;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Message;
import com.rotor.chappy.model.PendingMessages;
import com.rotor.chappy.utils.Docker;
import com.rotor.core.RFragment;
import com.rotor.core.Rotor;
import com.tapadoo.alerter.Alerter;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatFragment extends RFragment implements Frag, ChatInterface.View {

    private Toolbar toolbar;
    public ChatPresenter presenter;
    public MessageAdapter adapter;
    public PendingAdapter pendingAdapter;
    private RecyclerView messageList;
    private RecyclerView pendingMessageList;
    private Button sendButton;
    private EditText messageText;
    private PendingMessages pendingMessages;
    private boolean viewReady;
    private Runnable sender;
    private Handler handler;
    private long interval;


    @Nullable
    @Override
    public View onCreateRView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onRViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new ChatPresenter(this);

        toolbar = view.findViewById(R.id.toolbar);
        messageList = view.findViewById(com.rotor.chappy.R.id.messages_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(linearLayoutManager);
        adapter = new MessageAdapter(this);
        messageList.setAdapter(adapter);

        final LinearLayoutManager linearLayoutManagerP = new LinearLayoutManager(getActivity().getApplicationContext());
        pendingAdapter = new PendingAdapter(this);
        pendingMessageList = view.findViewById(R.id.pending_messages_list);
        pendingMessageList.setLayoutManager(linearLayoutManagerP);
        pendingMessageList.setAdapter(pendingAdapter);

        handler = new Handler();
        interval = 3000;
        sender = new Runnable() {
            @Override
            public void run() {
                if (presenter.chat() != null) {
                    pendingMessages = Docker.getPendingMessage(presenter.chat());
                    for (Map.Entry<String, Message> entry : pendingMessages.getMessages().entrySet()) {
                        presenter.chat().getMessages().put(entry.getKey(), entry.getValue());
                    }
                    if (!pendingMessages.getMessages().isEmpty()) {
                        presenter.updateChat();
                    }
                    handler.postDelayed(sender, interval);
                }
            }
        };

        messageText = view.findViewById(com.rotor.chappy.R.id.message_text);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    handled = true;
                    if (messageText.length() > 0 && presenter.chat() != null && Rotor.isConnected()) {
                        sendButton.performClick();
                    }
                }
                return handled;
            }
        });

        sendButton = view.findViewById(com.rotor.chappy.R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.getUser().getCurrentUser() != null && presenter.getUser().getCurrentUser().getUid() != null) {
                    Message message = new Message(presenter.getUser().getCurrentUser().getUid(), StringEscapeUtils.escapeJava(messageText.getText().toString()));
                    String id = String.valueOf(new Date().getTime());

                    Docker.addPendingMessage(presenter.chat(), id, message);
                    pendingMessages = Docker.getPendingMessage(presenter.chat());
                    pendingAdapter.notifyDataSetChanged();
                    handler.removeCallbacks(sender);
                    handler.postDelayed(sender, interval);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            messageText.setText("");
                            sendButton.setEnabled(messageText.length() > 0);
                        }
                    }, 100);
                }
            }
        });

        messageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.toString().length() > 0 && presenter.chat() != null && Rotor.isConnected());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do here
            }
        });

        sendButton.setEnabled(messageText.getText().toString().length() > 0 && presenter.chat() != null);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResumeView() {
        presenter.start();
        if (getActivity() != null) {
            ((HomeActivity)getActivity()).setSupportActionBar(toolbar);
        }
        viewReady = false;
        pendingMessages = null;
        handler.postDelayed(sender, interval);
    }

    @Override
    public void onPauseView() {
        presenter.stop();
        viewReady = false;
        handler.removeCallbacks(sender);
    }

    @Override
    public void onBackPressed() {
        App.setFragment(ChatsFragment.class);
        App.setCurrentChat(null);
    }

    @Override
    public void connected() {
        if (getActivity() != null) {
            Alerter.clearCurrent(getActivity());
        }
        sendButton.setEnabled(messageText.getText().length() > 0);
    }

    @Override
    public void disconnected() {
        sendButton.setEnabled(false);
        if (getActivity() != null) {
            Alerter.create(getActivity()).setTitle("Device not connected")
                    .setText("Trying to reconnect")
                    .enableProgress(true)
                    .disableOutsideTouch()
                    .enableInfiniteDuration(true)
                    .setProgressColorRes(R.color.primary)
                    .show();
        }
    }

    @Override
    public FragmentType type() {
        return FragmentType.CHAT;
    }

    @Override
    public String title() {
        return "Chats";
    }

    public static ChatFragment instance() {
        return new ChatFragment();
    }

    @Override
    public void chatDeleted() {
        onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_remove) {
            presenter.remove();
            return true;
        } else if (id == R.id.action_detail) {
            Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
            intent.putExtra("path", presenter.chat().getId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateUI(Chat chat) {
        if (presenter.chat() == null) {
            onBackPressed();
        }
        if (presenter.chat() != null) {
            toolbar.setTitle(StringEscapeUtils.unescapeJava(presenter.chat().getName()));
        }
        pendingMessages = Docker.getPendingMessage(presenter.chat());

        List<String> remove = new ArrayList<>();
        String[] messagesId = pendingMessages.getMessages().keySet().toArray(new String[0]);
        List<String> messagesIdChat = Arrays.asList(presenter.chat().getMessages().keySet().toArray(new String[0]));
        for (String toCheck : messagesId) {
            if (messagesIdChat.contains(toCheck)) {
                remove.add(toCheck);
            }
        }
        for (String toRemove : remove) {
            Docker.removePendingMessage(presenter.chat(), toRemove);
        }

        pendingMessages = Docker.getPendingMessage(presenter.chat());
        pendingAdapter.notifyDataSetChanged();
    }

    public List<Message> getPendingMessages() {
        if (pendingMessages == null && presenter.chat() != null) {
            pendingMessages = Docker.getPendingMessage(presenter.chat());
        } else if (pendingMessages == null && presenter.chat() == null) {
            pendingMessages = new PendingMessages();
        }
        return Arrays.asList(pendingMessages.getMessages().values().toArray(new Message[0]));
    }
}
