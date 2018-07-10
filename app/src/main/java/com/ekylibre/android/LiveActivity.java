package com.ekylibre.android;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.services.LocationService;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Rémi de Chazelles on 09/07/18.
 */
public class LiveActivity extends AppCompatActivity {

    private TextView speedTextView;
    private Intent serviceIntent;
    private AppDatabase database;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", MainActivity.LOCALE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        serviceIntent = new Intent(this, LocationService.class);
        database = AppDatabase.getInstance(this);

        speedTextView = findViewById(R.id.live_speed_value);

        startLocationService();


//        Polygon polygon = Polygon.fromJson(polyJson);
//        Log.e("GPS", "Polygon --> " + polygon);


//        TurfJoins.inside(Point.fromLngLat();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Attache the observer
        database.dao().getLastPoint().observeForever(position -> {
            speedTextView.setText(String.format(Locale.FRANCE, "%.1f km/h", position.speed * 3.6));
        });
    }

    private void startLocationService() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopLocationService() {
        stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopLocationService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationService();
                else
                    // Snackbar.make(mainLayout, "Permission denied", Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
