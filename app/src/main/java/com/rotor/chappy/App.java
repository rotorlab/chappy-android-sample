package com.rotor.chappy;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.efraespada.motiondetector.MotionDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
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

    public String type = "";
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

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                ImageLoader.getInstance().displayImage(uri.toString(), imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                // nothing to do here
            }

    /*
    @Override
    public Drawable placeholder(Context ctx) {
        return super.placeholder(ctx);
    }

    @Override
    public Drawable placeholder(Context ctx, String tag) {
        return super.placeholder(ctx, tag);
    }
    */
        });
    }

    public static Context context() {
        return context;
    }
}
