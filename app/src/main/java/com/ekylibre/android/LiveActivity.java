package com.ekylibre.android;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.services.LocationService;
import com.mapbox.turf.TurfJoins;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rémi de Chazelles on 09/07/18.
 */
public class LiveActivity extends AppCompatActivity {

    public static List<Crop> cropList;

    private TextView speedTextView;
    private TextView helpText;

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
        helpText = findViewById(R.id.live_help_text_bold);

        if (!isServiceRunning())
            new StartLocationService().execute();


//        Polygon polygon = Polygon.fromJson(polyJson);
//        Log.e("GPS", "Polygon --> " + polygon);


    }

    @Override
    protected void onResume() {
        super.onResume();

        // Attache the observer
        database.dao().getLastPoint().observeForever(position ->
            speedTextView.setText(String.format(Locale.FRANCE, "%.1f km/h", position.speed * 3.6))
        );
    }

    private void startLocationService() {
        startService(serviceIntent);
    }

    private void stopLocationService() {
        stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isServiceRunning())
            stopLocationService();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null)
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                if (LocationService.class.getName().equals(service.service.getClassName()))
                    return true;
        return false;
    }

    private class StartLocationService extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            cropList = database.dao().cropList(MainActivity.FARM_ID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (ContextCompat.checkSelfPermission(LiveActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LiveActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                startService(serviceIntent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (!isServiceRunning())
                        startService(serviceIntent);
                else
                    // Snackbar.make(mainLayout, "Permission denied", Snackbar.LENGTH_LONG).show();
                    break;
        }
    }

}
