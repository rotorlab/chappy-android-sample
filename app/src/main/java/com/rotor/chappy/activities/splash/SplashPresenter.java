package com.rotor.chappy.activities.splash;

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

public class SplashPresenter implements SplashInterface.Presenter {

    private SplashActivity view;
    private FirebaseAuth mAuth;
    private User user;
    private boolean omitMoreChanges;

    public SplashPresenter(SplashActivity view) {
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        if (mAuth.getUid() == null) {
            view.goLogin();
        } else {
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
                            SplashPresenter.this.user = new User(uid, name, email, photo, "android", Rotor.getId(), "", 0L, null);
                            Database.sync("/users/" + mAuth.getUid());
                        } else {
                            view.finish();
                        }
                    }

                    @Override
                    public void onChanged(@NonNull User ref) {
                        if (FirebaseAuth.getInstance().getUid() != null) {
                            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = u.getUid();
                            String name = u.getDisplayName();
                            String email = u.getEmail();
                            String photo = u.getPhotoUrl() != null ? u.getPhotoUrl().toString() : null;
                            user = ref;
                            user.setUid(uid);
                            user.setName(name);
                            user.setEmail(email);
                            user.setPhoto(photo);
                            user.setOs("android");
                            if (!Rotor.getId().equals(user.getToken())) {
                                user.setToken(Rotor.getId());
                                Database.sync("/users/" + mAuth.getUid());
                            } else if (!omitMoreChanges) {
                                Database.sync("/users/" + mAuth.getUid());
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
    }

    @Override
    public void goLogin() {
        view.goLogin();
    }

    @Override
    public void goMain() {
        view.goMain();
        App.listenUserPosition();
    }

}
