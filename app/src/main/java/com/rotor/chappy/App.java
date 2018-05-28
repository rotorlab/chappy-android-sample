package com.rotor.chappy;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import com.crashlytics.android.Crashlytics;
import com.efraespada.motiondetector.MotionDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.rotor.chappy.model.User;
import com.rotor.chappy.services.ChatRepository;
import com.rotor.chappy.services.ProfileRepository;
import com.rotor.database.Database;

import java.util.Date;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by efraespada on 07/03/2018.
 */

public class App extends Application {

    private static Context context;
    public static String databaseName = "database";
    public FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .build();

        ImageLoader.getInstance().init(config);

        auth = FirebaseAuth.getInstance();
        MotionDetector.initialize(this);
        MotionDetector.debug(true);
        MotionDetector.start(new com.efraespada.motiondetector.Listener() {
            @Override
            public void locationChanged(Location location) {
                if (auth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + auth.getCurrentUser().getUid());
                    if (user != null) {
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
            public void step() {
                if (auth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + auth.getCurrentUser().getUid());
                    if (user != null) {
                        user.setSteps(user.getSteps() + 1);
                        ProfileRepository.setUser("/users/" + user.getUid(), user);
                        Database.sync("/users/" + user.getUid());
                    }
                }
            }

            @Override
            public void type(String type) {
                if (auth.getCurrentUser() != null) {
                    User user = ProfileRepository.getUser("/users/" + auth.getCurrentUser().getUid());
                    if (user != null) {
                        user.setType(type);
                        ProfileRepository.setUser("/users/" + user.getUid(), user);
                        Database.sync("/users/" + user.getUid());
                    }
                }
            }
        });
    }

    public static Context context() {
        return context;
    }
}
