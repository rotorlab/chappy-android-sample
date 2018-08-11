package com.rotor.chappy.activities.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.rotor.chappy.BuildConfig;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.home.HomeActivity;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.activities.notifications.NotificationActivity;
import com.rotor.core.RAppCompatActivity;
import com.rotor.core.Rotor;
import com.rotor.core.interfaces.RStatus;
import com.rotor.database.Database;
import com.rotor.notifications.Notifications;
import com.rotor.notifications.interfaces.Listener;
import com.rotor.notifications.model.Notification;
import com.tapadoo.alerter.Alerter;

/**
 * Created by efraespada on 27/02/2018.
 */

public class SplashActivity extends RAppCompatActivity implements SplashInterface.View {

    public static int ACTION_CHAT = 4532;
    private SplashPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new SplashPresenter(this);

        Rotor.initialize(getApplicationContext(), BuildConfig.database_url, BuildConfig.redis_url, new RStatus() {
            @Override
            public void ready() {
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
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)  == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            presenter.goMain();
        } else {
            finish();
        }
    }

    @Override
    public void connected() {
        Alerter.clearCurrent(SplashActivity.this);
    }

    @Override
    public void disconnected() {
        Alerter.create(SplashActivity.this).setTitle("Device not connected")
                .setText("Trying to reconnect")
                .enableProgress(true)
                .disableOutsideTouch()
                .enableInfiniteDuration(true)
                .setProgressColorRes(R.color.primary)
                .show();
    }
}
