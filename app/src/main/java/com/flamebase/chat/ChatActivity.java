package com.flamebase.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flamebase.chat.model.GChat;
import com.flamebase.chat.model.Message;
import com.flamebase.chat.services.LocalData;
import com.flamebase.database.FlamebaseDatabase;
import com.flamebase.database.interfaces.ObjectBlower;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messageList;
    private GChat chat;
    private Button sendButton;
    private EditText messageText;
    private FlamebaseDatabase flamebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        final String path = intent.getStringExtra("path");

        FirebaseApp.initializeApp(this);
        LocalData.init(this);

        messageList = (RecyclerView) findViewById(R.id.messages_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(new MessageAdapter(this));

        messageText = (EditText) findViewById(R.id.message_text);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                String name = prefs.getString("name", null);
                if (name != null) {
                    Message message = new Message(name, messageText.getText().toString());
                    chat.getMessages().put(String.valueOf(new Date().getTime()), message);

                    flamebaseDatabase.sync();
                    messageText.setText("");
                    messageList.getAdapter().notifyDataSetChanged();
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        flamebaseDatabase = FlamebaseDatabase.getInstance().createListener(path, new ObjectBlower<GChat>() {

            @Override
            public GChat updateObject() {
                return chat;
            }

            @Override
            public void onObjectChanged(GChat ref) {
                if (chat == null) {
                    chat = ref;
                    ChatActivity.this.setTitle(ref.getName());
                } else {
                    chat.setName(ref.getName());
                    chat.setMessages(ref.getMessages());
                    chat.setMember(ref.getMember());
                }
                messageList.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void progress(int value) {

            }

        }, GChat.class);

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
    protected void onDestroy() {
        flamebaseDatabase.removeListener();
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

            Collections.sort(messages);

            String key = messages.get((messages.size() - 1) - position);

            Message message = chat.getMessages().get(key);

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
            content = (RelativeLayout) itemView.findViewById(R.id.message_content);
            text = (TextView) itemView.findViewById(R.id.message);
        }
    }

}
