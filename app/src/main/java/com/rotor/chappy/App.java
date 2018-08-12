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
import com.rotor.core.RViewPager;
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
    private static String currentChat;
    private static String currentProfile;
    private static RViewPager pager;
    private static int steps;

    public String type = "";
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        steps = -1;

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
    }

    public static Context context() {
        return context;
    }

    public static String getCurrentChat() {
        return currentChat;
    }

    public static void setCurrentChat(String currentChat) {
        App.currentChat = currentChat;
    }

    public static String getCurrentProfile() {
        return currentProfile;
    }

    public static void setCurrentProfile(String currentProfile) {
        App.currentProfile = currentProfile;
    }

    public static RViewPager getPager() {
        return pager;
    }

    public static void setPager(RViewPager pager) {
        App.pager = pager;
    }

    public static <T> void setFragment(Class<T> tClass) {
        pager.setFragment(tClass, false);
    }

    public static <T> void setFragment(Class<T> tClass, boolean transition) {
        pager.setFragment(tClass, transition);
    }

    public static void listenUserPosition() {
        MotionDetector.initialize(context());
        MotionDetector.setDebug(true);
        MotionDetector.setMinAccuracy(30);
        MotionDetector.setDeviceMustBeMoving(false);
        MotionDetector.start(new com.efraespada.motiondetector.Listener() {
            @Override
            public void locationChanged(Location location) {
                if (FirebaseAuth.getInstance().getUid() != null) {
                    User user = Database.backgroundHandler().getReference("/users/"
                            + FirebaseAuth.getInstance().getUid(), User.class);
                    if (user == null) return;
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
                    loc.setSteps((long) user.getSteps());
                    loc.setType(user.getType());
                    loc.setId(id);

                    com.rotor.chappy.model.Location lastLocation = user.getLastLocation();
                    if (lastLocation == null) {
                        user.getLocations().put(loc.getId(), loc);
                        Database.backgroundHandler().sync("/users/"
                                + FirebaseAuth.getInstance().getUid(), user);
                    } else if (lastLocation.getLatitude() != loc.getLatitude() || lastLocation.getLongitude() != loc.getLongitude()) {
                        user.getLocations().put(loc.getId(), loc);
                        Database.backgroundHandler().sync("/users/"
                                + FirebaseAuth.getInstance().getUid(), user);
                    }
                }
            }

            @Override
            public void accelerationChanged(float acceleration) {
                // nothing to do here
            }

            @Override
            public void locatedStep() {
                if (FirebaseAuth.getInstance().getUid() != null) {
                    User user = Database.backgroundHandler().getReference("/users/"
                            + FirebaseAuth.getInstance().getUid(), User.class);
                    if (user != null) {
                        if (steps == -1) {
                            steps = user.getSteps();
                        }
                        steps++;
                        user.setSteps(steps);
                        Database.backgroundHandler().sync("/users/"
                                + FirebaseAuth.getInstance().getUid(), user);
                    }
                }
            }

            @Override
            public void notLocatedStep() {
                if (FirebaseAuth.getInstance().getUid() != null) {
                    User user = Database.backgroundHandler().getReference("/users/"
                            + FirebaseAuth.getInstance().getUid(), User.class);
                    if (user != null) {
                        if (steps == -1) {
                            steps = user.getSteps();
                        }
                        steps++;
                        user.setSteps(steps);
                        Database.backgroundHandler().sync("/users/"
                                + FirebaseAuth.getInstance().getUid(), user);
                    }
                }
            }

            @Override
            public void type(String type) {
                if (FirebaseAuth.getInstance().getUid() != null) {
                    User user = Database.backgroundHandler().getReference("/users/"
                            + FirebaseAuth.getInstance().getUid(), User.class);
                    if (user != null) {
                        user.setType(type);
                        Database.backgroundHandler().sync("/users/"
                                + FirebaseAuth.getInstance().getUid(), user);
                    }
                }
            }
        });
    }

}
