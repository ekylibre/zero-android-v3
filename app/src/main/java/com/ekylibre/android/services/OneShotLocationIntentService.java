package com.ekylibre.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ResultReceiver;

import timber.log.Timber;


public class OneShotLocationIntentService extends IntentService {

    public static final String SINGLE_UPDATE = "com.ekylibre.android.services.action.SINGLE_UPDATE";
    public static final int OK = 1;

    private LocationManager locationManager = null;
    private ResultReceiver receiver;


    public OneShotLocationIntentService() {
        super("OneShotLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        receiver = intent.getParcelableExtra("receiver");

        final String action = intent.getAction();
        if (SINGLE_UPDATE.equals(action)) {
            handleGetSingleUpdte();
        }
    }

    private void handleGetSingleUpdte() {

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new SingleLocationListener(), null);
        } catch (java.lang.SecurityException ex) {
            Timber.e("fail to request location update, ignore --> %s", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Timber.e("gps provider does not exist %s", ex.getMessage());
        }


    }

    public class SingleLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getAccuracy() < 50) {
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", location.getLatitude());
                bundle.putDouble("longitude", location.getLongitude());
                locationManager.removeUpdates(this);
                receiver.send(OK, bundle);
            }
            else {
                Timber.i("Location is not accurate enougth...");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}
