package com.ekylibre.android.utils;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Rémi de Chazelles on 26/06/18.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new TimberLogTree());
    }
}
