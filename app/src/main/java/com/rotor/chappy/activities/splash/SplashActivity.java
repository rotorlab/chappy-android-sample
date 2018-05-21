package com.rotor.chappy.activities.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rotor.chappy.BuildConfig;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.activities.main.MainActivity;
import com.rotor.chappy.activities.notifications.NotificationActivity;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.Data;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.StatusListener;
import com.rotor.database.Database;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.interfaces.Listener;
import com.rotor.notifications.model.Notification;

/**
 * Created by efraespada on 27/02/2018.
 */

public class SplashActivity extends AppCompatActivity implements SplashInterface.View<User> {

    public static String TAG = SplashActivity.class.getSimpleName();
    public static int ACTION_CHAT = 4532;
    private SplashInterface.Presenter presenter;
    private User user;

    private boolean omitMoreChanges = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new SplashPresenter(this);

        LocalData.init(getApplicationContext());

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new StatusListener() {
            @Override
            public void connected() {
                Database.initialize();
                Notifications.initialize(NotificationActivity.class, new Listener() {
                    @Override
                    public void opened(@NonNull String deviceId, @NonNull Notification notification) {
                        Toast.makeText(getApplicationContext(), deviceId + " opened \"" + notification.getContent().getBody() + "\"", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void removed(@NonNull Notification notification) {

                    }
                });

                presenter.start();
            }

            @Override
            public void reconnecting() {

            }
        });
        Rotor.debug(true);
    }

    @Override
    public void goLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginGoogleActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void goMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreateReference() {
        presenter.goLogin();
    }

    @Override
    public void onReferenceChanged(User user) {
        this.user = user;
        if (!omitMoreChanges) {
            omitMoreChanges = true;
            Data.defineUser(user);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.goMain();
                }
            }, 3000);
        }
    }

    @Override
    public User onUpdateReference() {
        return user;
    }

    @Override
    public void onDestroyReference() {
        user = null;
        presenter.goLogin();
    }

    @Override
    public void progress(int value) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResumeView();
    }

    @Override
    protected void onPause() {
        presenter.onPauseView();
        super.onPause();
    }
}
