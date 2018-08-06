package com.rotor.chappy.activities.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.rotor.chappy.App;
import com.rotor.chappy.R;
import com.rotor.chappy.fragments.chat.ChatFragment;
import com.rotor.chappy.fragments.chats.ChatsFragment;
import com.rotor.chappy.fragments.map.MapFragment;

import com.rotor.chappy.fragments.profile.ProfileFragment;
import com.rotor.core.RFragment;
import com.rotor.core.RViewPager;

public class HomeActivity extends AppCompatActivity implements HomeInterface.View {

    private RFragment[] fragments = {
            ChatsFragment.instance(),
            ChatFragment.instance(),
            MapFragment.instance(),
            ProfileFragment.instance()
    };

    private RViewPager pager;
    private BottomNavigationViewEx navigationViewEx;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        pager = findViewById(R.id.pager);
        pager.init(this);
        App.setPager(pager);

        for (RFragment fragment : fragments) {
            pager.add(fragment);
        }

        pager.setFragment(ChatsFragment.class);

        navigationViewEx = findViewById(R.id.navigator);
        navigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_chats:
                        App.setFragment(ChatsFragment.class);
                        break;

                    case R.id.action_map:
                        App.setFragment(MapFragment.class);
                        break;

                    case R.id.action_profile:
                        App.setFragment(ProfileFragment.class);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        pager.onBackPressed();
    }

}
