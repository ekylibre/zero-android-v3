package com.ekylibre.android.utils;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import timber.log.Timber;

/**
 * Created by Rémi de Chazelles on 26/06/18.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new TimberLogTree());
    }

    // Override needed for low api
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
