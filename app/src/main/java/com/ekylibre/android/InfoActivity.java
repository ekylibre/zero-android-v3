package com.ekylibre.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ekylibre.android.adapters.CropInfo.CropItem;
import com.ekylibre.android.adapters.CropInfo.ListItem;
import com.ekylibre.android.adapters.CropInfo.ProductionItem;
import com.ekylibre.android.adapters.CropInfoAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.SimpleInterventions;
import com.ekylibre.android.fragments.InfoFragment;
import com.ekylibre.android.database.converters.Converters;
import com.ekylibre.android.services.SimpleLocationService;
import com.ekylibre.android.utils.RecyclerViewClickListener;
import com.ekylibre.android.utils.SimpleDividerItemDecoration;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;


public class InfoActivity extends AppCompatActivity implements InfoFragment.OnFragmentInteractionListener {

    public static final int FILTER_BY_PRODUCTION = 0;
    public static final int FILTER_BY_PROXIMITY = 1;
    public static int latestFilter = FILTER_BY_PRODUCTION;
    public static List<ListItem> dataset;
    public static RecyclerView.Adapter adapter;
    public static Snackbar snack;

    private TreeMap<String, Multimap> map;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Mes cultures");

        Timber.i("OnCreate()");

        map = new TreeMap<>();  // The map containing all crops with all infos for each
        dataset = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.crop_info_recycler);
//        ConstraintLayout layout = findViewById(R.id.info_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        RecyclerViewClickListener listener = (view, position) -> {

            CropItem cropItem = (CropItem) dataset.get(position);

            Bundle args = new Bundle();
            args.putString("full_name", cropItem.getName());
            args.putString("production", cropItem.getProduction());
            args.putString("start_date", Converters.toString(cropItem.getStartDate()));
            args.putString("stop_date", Converters.toString(cropItem.getStopDate()));
            args.putString("yield", cropItem.getYield());
            args.putFloat("surface", cropItem.getSurface());
            args.putString("uuid", cropItem.getUUID());

            ArrayList<Integer> interIDs = new ArrayList<>();
            for (Intervention inter : cropItem.getInterventions())
                interIDs.add(inter.id);
            args.putIntegerArrayList("interventionsIDs", interIDs);
            Timber.i("InterIDs --> %s", interIDs);

            InfoFragment infoFragment = InfoFragment.newInstance(args);
            infoFragment.show(getFragmentTransaction(), "dialog");
        };

        adapter = new CropInfoAdapter(this, dataset, listener);
        recyclerView.setAdapter(adapter);

        serviceIntent = new Intent(this, SimpleLocationService.class);

        snack = Snackbar.make(recyclerView, getString(R.string.gps_is_off), Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new RequestCropList().execute();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (isServiceRunning())
//            stopService(serviceIntent);
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceRunning())
            stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isServiceRunning())
            stopService(serviceIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        if (isServiceRunning())
            stopService(serviceIntent);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    public class RequestCropList extends AsyncTask<Void, Void, List<SimpleInterventions>> {

        Context context;
        String farm;

        RequestCropList() {
            this.context = getApplicationContext();
            this.farm = MainActivity.FARM_ID;
        }

        protected List<SimpleInterventions> doInBackground(Void... voids) {
            AppDatabase database = AppDatabase.getInstance(context);
            return database.dao().getSimpleInterventionList(farm);
        }

        @SuppressWarnings("unchecked")
        protected void onPostExecute(List<SimpleInterventions> result) {

            dataset.clear();

            for (SimpleInterventions item : result) {
                for (Crops inter : item.crops) {

                    // Get production nature from crop and build string
                    StringBuilder sb = new StringBuilder();
                    sb.append(inter.crop.get(0).production_nature);
                    String mode = inter.crop.get(0).production_mode;
                    if (mode.equals("Agriculture biologique") || mode.equals("Organic farming") )
                        sb.append(" bio");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(inter.crop.get(0).stop_date);
                    sb.append(" ").append(cal.get(Calendar.YEAR));

                    // Create on add crop for respective entry (production)
                    if (map.containsKey(sb.toString())) {
                        Multimap<Crop, Intervention> multimap = map.get(sb.toString());
                        multimap.put(inter.crop.get(0), item.intervention);
                    } else {
                        Multimap<Crop, Intervention> multimap = ArrayListMultimap.create();
                        multimap.put(inter.crop.get(0), item.intervention);
                        map.put(sb.toString(), multimap);
                    }
                }
            }

            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                // Add header to the dataset
                ProductionItem header = new ProductionItem();
                header.setName(pair.getKey().toString());
                dataset.add(header);

                // Add child items to this header
                Multimap<Crop, Intervention> cropsInProd = (Multimap) pair.getValue();
                for (Crop crop : cropsInProd.keySet()) {
                    List<Intervention> interList = new ArrayList<>(cropsInProd.get(crop));
                    CropItem cropItem = new CropItem(crop.name, crop.uuid, crop.production_nature,
                            crop.surface_area, crop.start_date, crop.stop_date,
                            crop.provisional_yield, crop.centroid, interList);
                    dataset.add(cropItem);
                }
                it.remove();
            }
            adapter.notifyDataSetChanged();

            if (latestFilter == FILTER_BY_PROXIMITY && !isServiceRunning())
                startService(serviceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_activity, menu);
        boolean status = latestFilter == FILTER_BY_PRODUCTION;
        menu.findItem(R.id.filter_by_production).setEnabled(!status);
        menu.findItem(R.id.filter_by_proximity).setEnabled(status);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.filter_by_production:
                latestFilter = FILTER_BY_PRODUCTION;
                if (snack.isShown())
                    snack.dismiss();
                if (isServiceRunning())
                    stopService(serviceIntent);
                if (dataset.isEmpty())
                    new RequestCropList().execute();
                else
                    adapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;

            case R.id.filter_by_proximity:

                latestFilter = FILTER_BY_PROXIMITY;

                if (ContextCompat.checkSelfPermission(InfoActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(InfoActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    if (!isServiceRunning()) {
                        snack.setText(getString(R.string.waiting_gps_signal)).show();
                        startService(serviceIntent);
                    } else
                        Timber.i("Service is already running...");

                    invalidateOptionsMenu();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null)
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                if (SimpleLocationService.class.getName().equals(service.service.getClassName()))
                    return true;
        return false;
    }

    @Override
    public void onFragmentInteraction(Object selection) {

    }

    public FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null)
            ft.remove(prev);
        ft.addToBackStack(null);
        return ft;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!isServiceRunning())
                        snack.setText(getString(R.string.waiting_gps_signal)).show();
                        startService(serviceIntent);
                } else {
                    latestFilter = FILTER_BY_PROXIMITY;
                    invalidateOptionsMenu();
                }
                    // Snackbar.make(mainLayout, "Permission denied", Snackbar.LENGTH_LONG).show();
                break;
        }
    }
}
