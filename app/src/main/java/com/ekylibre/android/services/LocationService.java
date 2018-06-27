package com.ekylibre.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import timber.log.Timber;


public class LocationService extends Service {

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider)
        {
            Timber.e("LocationListener %s", provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Timber.e("onLocationChanged: %s", location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Timber.e("onProviderDisabled: %s", provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Timber.e("onProviderEnabled: %s", provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Timber.e("onStatusChanged: %s", provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Timber.e("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Timber.e("onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Timber.i(ex, "fail to request location update, ignore");
        } catch (IllegalArgumentException ex) {
            Timber.d("network provider does not exist, %s", ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Timber.i(ex, "fail to request location update, ignore");
        } catch (IllegalArgumentException ex) {
            Timber.d("gps provider does not exist %s", ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Timber.e("onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Timber.i(ex, "fail to remove location listners, ignore");
                }
            }
        }
    }

    private void initializeLocationManager() {
        Timber.e("initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}