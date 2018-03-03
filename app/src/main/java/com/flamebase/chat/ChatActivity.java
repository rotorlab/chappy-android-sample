package com.flamebase.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

import com.flamebase.chat.model.Chat;
import com.flamebase.chat.model.Message;
import com.flamebase.chat.services.LocalData;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.interfaces.ObjectBlower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messageList;
    private Chat chat;
    private Button sendButton;
    private EditText messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        final String path = "/chats/" + intent.getStringExtra("path").replaceAll(" ", "_");

        messageList = (RecyclerView) findViewById(R.id.messages_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(new MessageAdapter(this));

        messageText = findViewById(R.id.message_text);
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

        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                String name = prefs.getString("name", null);
                if (name != null) {
                    Message message = new Message(name, messageText.getText().toString());
                    chat.getMessages().put(String.valueOf(new Date().getTime()), message);

                    FlamebaseDatabase.sync(path);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            messageText.setText("");
                            sendButton.setEnabled(messageText.length() > 0);
                        }
                    }, 100);


                    // messageList.getAdapter().notifyDataSetChanged();
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FlamebaseDatabase.createListener(path, new ObjectBlower<Chat>() {

            @Override
            public Chat updateObject() {
                return chat;
            }

            @Override
            public void onObjectChanged(Chat ref) {
                sendButton.setEnabled(ref != null);
                if (ref != null) {
                    chat = ref;
                }

                if (chat != null) {
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
                }

                sendButton.setEnabled(messageText.toString().length() > 0 && chat != null);
            }

            @Override
            public void progress(int value) {

            }

        }, Chat.class);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create_group) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FlamebaseDatabase.onResume();
        sendButton.setEnabled(messageText.getText().toString().length() > 0 && chat != null);
    }

    @Override
    protected void onPause() {
        FlamebaseDatabase.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //flamebaseDatabase.removeListener();
        super.onDestroy();
    }

    public class MessageAdapter extends RecyclerView.Adapter<ViewHolder> {

        public Context context;

        public MessageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            List<String> messages = new ArrayList<>();
            for (Map.Entry<String, Message> entry : chat.getMessages().entrySet()) {
                messages.add(entry.getKey());
            }

            Message message = chat.getMessages().get(messages.get((messages.size() - 1) - position));

            holder.text.setText(message.getText());
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout content;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.message_content);
            text = itemView.findViewById(R.id.message);
        }
    }

}
