package com.rotor.chappy.activities.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.chat.ChatActivity;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.activities.profile.ProfileActivity;
import com.rotor.chappy.adapters.ChatAdapter;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.core.Rotor;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MainInterface.View<Chat>, ProfilesView {

    private MaterialDialog materialDialog;
    private RecyclerView chatsList;
    private MainPresenter presenter;
    private Map<String, Chat> chats;
    private Map<String, User> profiles;
    private Toolbar toolbar;
    private boolean uiReady;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this, this);
        uiReady = false;
        chats = new HashMap<>();
        profiles = new HashMap<>();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatsList = findViewById(R.id.chats_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chatsList.setLayoutManager(mLayoutManager);

        chatsList.setAdapter(new ChatAdapter() {
            @Override
            public void onChatClicked(Chat chat) {
                presenter.goToChat(chat);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForGroupName();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResumeView();
        Rotor.onResume();
        presenter.prepareChatsFor();
        if (chatsList != null) {
            chatsList.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        Rotor.onPause();
        presenter.onPauseView();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create_group) {
            askForGroupName();
            return true;
        } else if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, LoginGoogleActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void askForGroupName() {
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }

        materialDialog = new MaterialDialog.Builder(this)
                .title(com.rotor.chappy.R.string.askIdGroupName)
                .customView(R.layout.input_group_name, true)
                .positiveText(com.rotor.chappy.R.string.agree)
                .negativeText(com.rotor.chappy.R.string.disagree)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final EditText name = dialog.getCustomView().findViewById(R.id.etName);
                        if (!TextUtils.isEmpty(name.getText())) {
                            Chat chat = presenter.createChat(StringEscapeUtils.escapeJava(name.getText().toString()));
                            String path = "/chats/" + chat.getId();
                            chats.put(path, chat);
                            presenter.prepareFor(path, Chat.class);
                            dialog.dismiss();
                            materialDialog = null;
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        materialDialog = null;
                    }
                })
                .show();
    }


    @Override
    public void openChat(Chat chat) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("path", chat.getId());
        startActivity(intent);
    }

    @Override
    public void refreshUI() {
        ((ChatAdapter) chatsList.getAdapter()).chats.clear();
        ((ChatAdapter) chatsList.getAdapter()).chats.addAll(chats.values());
        chatsList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onCreateReference(String key) {
        // should be called
    }

    @Override
    public void onReferenceChanged(String key, Chat chat) {
        chats.put(key, chat);
        presenter.refreshUI();
    }

    @Override
    public Chat onUpdateReference(String key) {
        return chats.get(key);
    }

    @Override
    public void onDestroyReference(String key) {
        chats.remove(key);
        presenter.refreshUI();
    }

    @Override
    public void progress(String key, int value) {

    }

    @Override
    public void onCreateUser(String key) {
        // should not be called
    }

    @Override
    public void onUserChanged(String key, User user) {
        profiles.put(key, user);
        if (user.getUid().equals(presenter.getLoggedUid()) && !uiReady) {
            uiReady = true;

            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.header)
                    .withSelectionListEnabledForSingleProfile(false)
                    .addProfiles(
                            new ProfileDrawerItem().withName(user.getName()).withEmail(user.getEmail()).withIcon(user.getPhoto())
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();

            PrimaryDrawerItem profileButton = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.menu_label_profile).withIcon(GoogleMaterial.Icon.gmd_account_circle);
            PrimaryDrawerItem chatsButton = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.menu_label_chats).withIcon(GoogleMaterial.Icon.gmd_message);
            SecondaryDrawerItem settingsButton = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.menu_label_settings).withIcon(GoogleMaterial.Icon.gmd_settings);
            SecondaryDrawerItem logoutButton = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.menu_label_sign_out).withIcon(GoogleMaterial.Icon.gmd_exit_to_app);

            DrawerBuilder builder = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withAccountHeader(headerResult)
                    .addDrawerItems(
                            profileButton,
                            chatsButton,
                            new DividerDrawerItem(),
                            settingsButton,
                            logoutButton
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            // do something with the clicked item :D
                            int id = (int) drawerItem.getIdentifier();
                            switch (id) {
                                case 1:
                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                    return true;
                                case 2:
                                    return true;

                                case 4:
                                    AuthUI.getInstance()
                                            .signOut(MainActivity.this)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Intent intent = new Intent(MainActivity.this, LoginGoogleActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            Drawer drawer = builder.build();

            drawer.setSelection(chatsButton);
        }
    }

    @Override
    public User onUpdateUser(String key) {
        return profiles.get(key);
    }

    @Override
    public void onDestroyUser(String key) {
        profiles.remove(key);
    }

    @Override
    public void userProgress(String key, int value) {
        // nothing to do here
    }
}
