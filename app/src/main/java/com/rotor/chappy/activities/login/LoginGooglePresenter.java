package com.rotor.chappy.activities.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.App;
import com.rotor.chappy.model.User;
import com.rotor.core.Rotor;
import com.rotor.database.Database;
import com.rotor.database.abstr.Reference;

import static com.rotor.chappy.activities.login.LoginGoogleActivity.LOCATION_REQUEST_CODE;

public class LoginGooglePresenter implements LoginGoogleInterface.Presenter {

    private LoginGoogleActivity view;
    private FirebaseAuth mAuth;
    private User user;
    private boolean omitMoreChanges;

    public LoginGooglePresenter(LoginGoogleActivity view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.omitMoreChanges = false;
    }

    @Override
    public void start() {
        if (mAuth.getUid() != null){
            Database.listen(App.databaseName, "/users/" + mAuth.getUid(), new Reference<User>(User.class) {
                @Override
                public void onCreate() {
                    if (FirebaseAuth.getInstance().getUid() != null) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        String photo = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                        LoginGooglePresenter.this.user = new User(uid, name, email, photo, "android", Rotor.getId(), "", 0L, null);
                        Database.sync("/users/" + mAuth.getUid());
                    } else {
                        view.finish();
                    }
                }

                @Override
                public void onChanged(@NonNull User ref) {
                    user = ref;
                    if (!Rotor.getId().equals(user.getToken())) {
                        user.setToken(Rotor.getId());
                    } else if (!omitMoreChanges) {
                        omitMoreChanges = true;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(view, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(view, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(view, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                goMain();
                            } else {
                                String[] perm = new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.CAMERA
                                };
                                ActivityCompat.requestPermissions(view, perm, LOCATION_REQUEST_CODE);
                            }
                        } else {
                            goMain();
                        }
                    }
                }

                @Nullable
                @Override
                public User onUpdate() {
                    return user;
                }

                @Override
                public void onDestroy() {
                    // shouldn't be called
                }

                @Override
                public void progress(int value) {
                    // shouldn't be called
                }

            });

        }
    }

    @Override
    public void goMain() {
        App.listenUserPosition();
        view.goMain();
    }

    @Override
    public User user() {
        return user;
    }


}
