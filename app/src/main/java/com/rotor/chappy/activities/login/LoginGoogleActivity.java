package com.rotor.chappy.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.main.MainActivity;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.Data;
import com.rotor.core.Rotor;

import java.util.Arrays;
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
        user = new User(uid, name, email, photo, "android", Rotor.getId());
        presenter.sync("/users/" + uid);
    }

    @Override
    public void onReferenceChanged(User user) {
        if (!omitMoreChanges) {
            omitMoreChanges = true;
            presenter.sayHello(user);
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

    }

    @Override
    public void progress(int value) {

    }

    @Override
    protected void onDestroy() {
        presenter.onPauseView();
        super.onDestroy();
    }
}