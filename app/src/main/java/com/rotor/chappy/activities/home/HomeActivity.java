package com.rotor.chappy.activities.home;

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
import com.rotor.chappy.activities.main.MainInterface;
import com.rotor.chappy.activities.main.MainPresenter;
import com.rotor.chappy.activities.profile.ProfileActivity;
import com.rotor.chappy.adapters.ChatAdapter;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilesView;
import com.rotor.core.RAppCompatActivity;
import com.tapadoo.alerter.Alerter;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements HomeInterface.View {


    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }



    @Override
    public void goTo(FragmentType fragmentType) {

    }

    @Override
    public void logout() {

    }
}
