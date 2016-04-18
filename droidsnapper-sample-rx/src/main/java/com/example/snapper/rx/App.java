package com.example.snapper.rx;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Static helper
    ///////////////////////////////////////////////////////////////////////////

    public static App from(Context context) {
        return (App) context.getApplicationContext();
    }

    private static App instance;

    public static App instance() {
        return instance;
    }
}
