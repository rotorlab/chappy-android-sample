package com.rotor.chappy.activities.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.home.HomeActivity;
import com.rotor.core.RAppCompatActivity;
import com.tapadoo.alerter.Alerter;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class LoginGoogleActivity extends RAppCompatActivity implements LoginGoogleInterface.View {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private LoginGoogleInterface.Presenter presenter;


    public static final int LOCATION_REQUEST_CODE = 2345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        presenter = new LoginGooglePresenter(this);

        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void goMain() {
        Intent intent = new Intent(LoginGoogleActivity.this, HomeActivity.class);
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
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenter.start();
        }
    }

    @Override
    public void connected() {
        Alerter.clearCurrent(LoginGoogleActivity.this);
    }

    @Override
    public void disconnected() {
        Alerter.create(LoginGoogleActivity.this).setTitle("Device not connected")
                .setText("Trying to reconnect")
                .enableProgress(true)
                .disableOutsideTouch()
                .enableInfiniteDuration(true)
                .setProgressColorRes(R.color.primary)
                .show();
    }
}