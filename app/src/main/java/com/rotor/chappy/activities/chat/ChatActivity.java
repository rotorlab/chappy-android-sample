package com.rotor.chappy.activities.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.chat_detail.ChatDetailActivity;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.Member;
import com.rotor.chappy.model.Message;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.core.Rotor;
import com.rotor.notifications.Notifications;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatActivity extends AppCompatActivity implements ChatInterface.View<Chat>, ProfilesView {

    private RecyclerView messageList;
    private Chat chat;
    private Button sendButton;
    private EditText messageText;
    private String path;
    private static final Map<String, User> users = new HashMap<>();

    private ChatPresenter<Chat> presenter;
    private FirebaseAuth mAuth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.rotor.chappy.R.layout.activity_chat);
        Toolbar toolbar = findViewById(com.rotor.chappy.R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new ChatPresenter<>(this, this);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            finish();
        }

        Intent intent = getIntent();

        path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        messageList = findViewById(com.rotor.chappy.R.id.messages_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(new MessageAdapter());

        messageText = findViewById(com.rotor.chappy.R.id.message_text);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    handled = true;
                    if (messageText.length() > 0) {
                        sendButton.performClick();
                    }
                }
                return handled;
            }
        });

        sendButton = findViewById(com.rotor.chappy.R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid() != null) {
                    Message message = new Message(mAuth.getCurrentUser().getUid(), StringEscapeUtils.escapeJava(messageText.getText().toString()));
                    chat.getMessages().put(String.valueOf(new Date().getTime()), message);

                    presenter.sync(path);

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

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.toString().length() > 0 && chat != null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setEnabled(messageText.getText().toString().length() > 0 && chat != null);

        FloatingActionButton fab = findViewById(com.rotor.chappy.R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        Notifications.remove(intent.getStringExtra("path"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove) {
            presenter.remove(path);
            return true;
        } else if (id == R.id.action_detail) {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("path", chat.getId());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Rotor.onResume();
        presenter.onResumeView();
        presenter.prepareFor(path, Chat.class);
        sendButton.setEnabled(messageText.getText().toString().length() > 0 && chat != null);
    }

    @Override
    protected void onPause() {
        presenter.onPauseView();
        Rotor.onPause();
        super.onPause();
    }

    @Override
    public void onCreateReference() {
        finish();
    }

    @Override
    public void onReferenceChanged(Chat chat) {
        ChatActivity.this.chat = chat;

        for (Map.Entry<String, Member> entry : chat.getMembers().entrySet()) {
            if (!users.containsKey("/users/" + entry.getValue().getId())) {
                presenter.prepareProfileFor("/users/" + entry.getValue().getId());
            }
        }

        ChatActivity.this.setTitle(chat.getName());
        Map<String, Message> messageMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Long a = Long.valueOf(o1);
                Long b = Long.valueOf(o2);
                if (a > b) {
                    return 1;
                } else if (a < b) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        messageMap.putAll(chat.getMessages());

        chat.setMessages(messageMap);

        messageList.getAdapter().notifyDataSetChanged();

        messageList.smoothScrollToPosition(0);

        sendButton.setEnabled(messageText.toString().length() > 0);
    }

    @Override
    public Chat onUpdateReference() {
        return ChatActivity.this.chat;
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
        // should be called
    }

    @Override
    public void onUserChanged(String key, User user) {
        users.put(key, user);
        messageList.getAdapter().notifyDataSetChanged();
        messageList.smoothScrollToPosition(0);
    }

    @Override
    public User onUpdateUser(String key) {
        return users.get(key);
    }

    @Override
    public void onDestroyUser(String key) {
        users.remove(key);
    }

    @Override
    public void userProgress(String key, int value) {
        // nothing to do here
    }

    public class MessageAdapter extends RecyclerView.Adapter<VHMessages> {

        private MessageAdapter() {
            // nothing to do here
        }

        @Override
        @NonNull
        public VHMessages onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new VHMessages(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull VHMessages holder, int position) {
            List<String> messages = new ArrayList<>();
            for (Map.Entry<String, Message> entry : chat.getMessages().entrySet()) {
                messages.add(entry.getKey());
            }

            Message message = chat.getMessages().get(messages.get((messages.size() - 1) - position));

            if (users.containsKey("/users/" + message.getAuthor())) {
                User user = users.get("/users/" + message.getAuthor());
                holder.author.setText(user.getName() + ":");
                holder.message.setText(StringEscapeUtils.unescapeJava(message.getText()));
                if (presenter.getLoggedUid() != null) {

                }
                ImageLoader.getInstance().displayImage(user.getPhoto(), holder.image);
            }
        }

        @Override
        public int getItemCount() {
            if (chat == null || chat.getMessages() == null) {
                return 0;
            } else {
                return chat.getMessages().size();
            }
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
