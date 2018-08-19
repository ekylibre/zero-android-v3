package com.ekylibre.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.widget.Toast;

import com.ekylibre.android.LiveActivity;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Point;
import com.mapbox.turf.TurfJoins;

import timber.log.Timber;


public class LocationService extends Service {

    private static final int INTERVAL = 1000;
    private static final int DISTANCE = 1;

    private LocationManager locationManager = null;
    private AppDatabase database;
    public static boolean writeDatabase = false;

    private class LocationListener implements android.location.LocationListener {

        Location location;

        LocationListener(String provider) {
            location = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (writeDatabase) {
                new WriteDatabaseTask(location).execute();
            }
            for (Crop crop : LiveActivity.cropList) {
                if (TurfJoins.inside(
                        com.mapbox.geojson.Point.fromLngLat(
                                location.getLongitude(), location.getLatitude()), crop.shape)) {
                    Timber.i("Tu es dans le polygon %s", crop.name);
                    break;
                }

            }
            Timber.i("onLocationChanged: %s %s %s %s", location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime());
            this.location.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Timber.i("GPS disabled");
            Toast toast = Toast.makeText(getBaseContext(), "Le GPS de votre smartphone est éteint...", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 200);
            toast.show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Timber.i("GPS enabled");
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
        Timber.e("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        Timber.d("onCreate");

        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener);
        } catch (java.lang.SecurityException ex) {
            Timber.e("fail to request location update, ignore --> %s", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Timber.e("gps provider does not exist %s", ex.getMessage());
        }
        database = AppDatabase.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Timber.e("onDestroy");
        super.onDestroy();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Timber.e("fail to remove location listeners, ignore%s", ex.getMessage());
            }
        }
    }

    private void initializeLocationManager() {
        Timber.e("initializeLocationManager");
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