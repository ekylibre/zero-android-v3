package com.ekylibre.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;

import com.ekylibre.android.InfoActivity;
import com.ekylibre.android.adapters.CropInfo.CropItem;
import com.ekylibre.android.adapters.CropInfo.ListItem;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

import static com.ekylibre.android.InfoActivity.FILTER_BY_PROXIMITY;


public class SimpleLocationService extends Service {

    private static final int INTERVAL = 10000;  // 10 sec
    private static final int DISTANCE = 1;

    private LocationManager locationManager = null;

    private class LocationListener implements android.location.LocationListener {

        Location location;

        LocationListener(String provider) {
            location = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {

            if (location.getAccuracy() < 50) {
                Point point = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                // Get a list of headers index and calcul distances
                List<Integer> headerIndex = new ArrayList<>();
                for (ListItem item : InfoActivity.dataset) {
                    if (item.getType() == ListItem.TYPE_HEADER) {
                        headerIndex.add(InfoActivity.dataset.indexOf(item));
                    } else {
                        CropItem crop = (CropItem) item;
                        Double dist = TurfMeasurement.distance(point, crop.getCentroid(), TurfConstants.UNIT_METERS);
                        ((CropItem) item).setDistance(dist);
                    }
                }

                int previous = 0;
                for (Integer toIndex : headerIndex.subList(1,headerIndex.size())) {

                    int fromIndex = previous + 1;
                    List<ListItem> sublist = new ArrayList<>(InfoActivity.dataset.subList(fromIndex, toIndex));

                    Collections.sort(sublist, (o1, o2) -> {
                        CropItem obj1 = (CropItem) o1;
                        CropItem obj2 = (CropItem) o2;
                        return obj1.getDistance().compareTo(obj2.getDistance());
                    });

                    int itemIdx = fromIndex;
                    for (ListItem item : sublist) {
                        InfoActivity.dataset.set(itemIdx, item);
                        itemIdx += 1;
                    }

                    previous = toIndex;
                }

                InfoActivity.adapter.notifyDataSetChanged();
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
            InfoActivity.latestFilter = FILTER_BY_PROXIMITY;
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener);
        } catch (SecurityException ex) {
            Timber.e("fail to request location update, ignore --> %s", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Timber.e("gps provider does not exist %s", ex.getMessage());
        }
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
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
}