package com.rotor.chappy.activities.splash;

import android.location.Location;

import com.efraespada.motiondetector.MotionDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rotor.chappy.App;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.mpv.ProfilePresenter;
import com.rotor.chappy.model.mpv.ProfileView;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;
import com.rotor.database.Database;

import java.util.Date;
import java.util.HashMap;

public class SplashPresenter implements SplashInterface.Presenter, ProfilePresenter {

    private SplashActivity view;
    private ProfileRepository profileRepository;
    private FirebaseAuth mAuth;
    private boolean visible;
    private String type;

    public SplashPresenter(SplashActivity view) {
        this.view = view;
        this.profileRepository = new ProfileRepository();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            view.goLogin();
        } else {
            prepareProfileFor("/users/" + user.getUid());
        }
    }

    @Override
    public void goLogin() {
        view.goLogin();
    }

    @Override
    public void goMain() {
        view.goMain();
        MotionDetector.initialize(view);
        MotionDetector.debug(true);
        MotionDetector.minAccuracy(30);
        MotionDetector.start(new com.efraespada.motiondetector.Listener() {
            @Override
            public void locationChanged(Location location) {
                if (mAuth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + mAuth.getCurrentUser().getUid());
                    if (user != null) {
                        if (user.getLocations() == null) {
                            user.setLocations(new HashMap<String, com.rotor.chappy.model.Location>());
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

                        user.getLocations().put(loc.getId(), loc);
                        ProfileRepository.setUser("/users/" + user.getUid(), user);
                    }
                }
            }

            @Override
            public void accelerationChanged(float acceleration) {
                // nothing to do here
            }

            @Override
            public void locatedStep() {
                if (mAuth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + mAuth.getCurrentUser().getUid());
                    if (user != null) {
                        user.setSteps(user.getSteps() + 1);
                        ProfileRepository.setUser("/users/" + user.getUid(), user);
                        Database.sync("/users/" + user.getUid());
                    }
                }
            }

            @Override
            public void notLocatedStep() {
                if (mAuth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + mAuth.getCurrentUser().getUid());
                    if (user != null) {
                        user.setSteps(user.getSteps() + 1);
                        ProfileRepository.setUser("/users/" + user.getUid(), user);
                        Database.sync("/users/" + user.getUid());
                    }
                }
            }

            @Override
            public void type(String type) {
                if (!type.equals(SplashPresenter.this.type)) {
                    SplashPresenter.this.type = type;
                    if (mAuth.getCurrentUser() != null) {
                        User user = ProfileRepository.getUser("/users/" + mAuth.getCurrentUser().getUid());
                        if (user != null) {
                            user.setType(type);
                            ProfileRepository.setUser("/users/" + user.getUid(), user);
                            Database.sync("/users/" + user.getUid());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResumeView() {
        visible = true;
    }

    @Override
    public void onPauseView() {
        visible = false;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void prepareProfileFor(String id) {
        profileRepository.listen(id, this, view);
    }

    @Override
    public void syncProfile(String id) {
        profileRepository.sync(id);
    }

    @Override
    public void removeProfile(String id) {
        profileRepository.remove(id);
    }

    @Override
    public String getLoggedUid() {
        return mAuth != null && mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }
}
