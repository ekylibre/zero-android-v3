package com.ekylibre.android.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Point;


@SuppressLint("LogNotTimber")
public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final int INTERVAL = 1000;
    private static final int DISTANCE = 1;

    private LocationManager locationManager = null;
    private AppDatabase database;

    private class LocationListener implements android.location.LocationListener {

        Location location;

        LocationListener(String provider) {
            location = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            new WriteDatabaseTask(location).execute();
            Log.i(TAG, String.format("onLocationChanged: %s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime()));
            this.location.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "GPS disabled");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "GPS enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    LocationListener locationListener = new LocationListener(LocationManager.GPS_PROVIDER);


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate");

        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG,"fail to request location update, ignore --> " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }
        database = AppDatabase.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Log.e(TAG,"fail to remove location listeners, ignore" + ex.getMessage());
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private class WriteDatabaseTask extends AsyncTask<Void, Void, Void> {

        private Location location;

        WriteDatabaseTask(Location location) {
            this.location = location;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            database.dao().insert(
                    new Point(location.getTime(), location.getLatitude(), location.getLongitude(),
                            Math.round(location.getSpeed()*36f)/10.0f, (int) location.getAccuracy(), null, 0));

            return null;
        }
    }
}