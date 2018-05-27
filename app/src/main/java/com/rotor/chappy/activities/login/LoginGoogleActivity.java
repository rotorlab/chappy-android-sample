package com.rotor.chappy.activities.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.efraespada.motiondetector.MotionDetector;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.main.MainActivity;
import com.rotor.chappy.model.Location;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.core.Rotor;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class LoginGoogleActivity extends AppCompatActivity implements LoginGoogleInterface.View<User> {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private LoginGoogleInterface.Presenter<User> presenter;

    private boolean omitMoreChanges = false;
    private User user;

    private int LOCATION_REQUEST_CODE = 2345;

    private String uid;
    private String name;
    private String email;
    private String photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        presenter = new LoginGooglePresenter(this);

        presenter.onResumeView();

        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                uid = user.getUid();
                name = user.getDisplayName();
                email = user.getEmail();
                photo = user.getPhotoUrl().toString();

                presenter.prepareFor("/users/" + uid, User.class);
                presenter.sync("/users/" + uid);
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    @Override
    public void sayHello(User user) {

    }

    @Override
    public void goMain() {
        Intent intent = new Intent(LoginGoogleActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreateReference() {
        user = new User(uid, name, email, photo, "android", Rotor.getId(), "", 0L, new HashMap<String, Location>());
        presenter.sync("/users/" + uid);
    }

    @Override
    public void onReferenceChanged(User user) {
        if (!omitMoreChanges) {
            omitMoreChanges = true;
            presenter.sayHello(user);
            ChatRepository.defineUser(user);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    String[] perm = new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    };
                    ActivityCompat.requestPermissions(this, perm, LOCATION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public User onUpdateReference() {
        return user;
    }

    @Override
    public void onDestroyReference() {

    }

    @Override
    public void progress(int value) {

    }

    private void startService() {
        MotionDetector.start(new com.efraespada.motiondetector.Listener() {
            @Override
            public void locationChanged(android.location.Location location) {
                if (ChatRepository.getUser().getLocations() == null) {
                    ChatRepository.getUser().setLocations(new HashMap<String, com.rotor.chappy.model.Location>());
                }
                String id = new Date().getTime() + "";
                com.rotor.chappy.model.Location loc = new com.rotor.chappy.model.Location();
                loc.setAccuracy(location.getAccuracy());
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                loc.setAltitude(location.getAltitude());
                loc.setSpeed(location.getSpeed());
                loc.setSteps(user.getSteps());
                loc.setType(ChatRepository.getUser().getType());
                loc.setId(id);

                ChatRepository.getUser().getLocations().put(loc.getId(), loc);
            }

            @Override
            public void accelerationChanged(float acceleration) {
                // nothing to do here
            }

            @Override
            public void step() {
                ChatRepository.getUser().setSteps(ChatRepository.getUser().getSteps() + 1);
            }

            @Override
            public void type(String type) {
                ChatRepository.getUser().setType(type);
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                presenter.goMain();
            }
        }, 3000);
    }


    @Override
    protected void onDestroy() {
        presenter.onPauseView();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            startService();
        } else {
            finish();
        }
    }
}