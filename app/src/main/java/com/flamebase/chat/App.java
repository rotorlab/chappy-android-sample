package com.flamebase.chat;

import android.app.Application;
import android.content.Context;

/**
 * Created by efraespada on 07/03/2018.
 */

public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context context() {
        return context;
    }
}
