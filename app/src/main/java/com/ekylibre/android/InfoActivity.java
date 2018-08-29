package com.ekylibre.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ekylibre.android.adapters.CropInfo.CropItem;
import com.ekylibre.android.adapters.CropInfo.ListItem;
import com.ekylibre.android.adapters.CropInfo.ProductionItem;
import com.ekylibre.android.adapters.CropInfoAdapter;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.pojos.Crops;
import com.ekylibre.android.database.pojos.SimpleInterventions;
import com.ekylibre.android.services.LocationService;
import com.ekylibre.android.services.OneShotLocationIntentService;
import com.ekylibre.android.services.ServiceResultReceiver;
import com.ekylibre.android.utils.Converters;
import com.ekylibre.android.utils.RecyclerViewClickListener;
import com.ekylibre.android.utils.SimpleDividerItemDecoration;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class InfoActivity extends AppCompatActivity
        implements InfoFragment.OnFragmentInteractionListener, ServiceResultReceiver.Receiver {

    private static final int FILTER_BY_PRODUCTION = 0;
    private static final int FILTER_BY_PROXIMITY = 1;

    private HashMap<String, Multimap> map;
    private List<ListItem> dataset;
    private RecyclerView.Adapter adapter;
    private Intent serviceIntent;
    private ServiceResultReceiver resultReceiver;
    private Location currentLocation;

    public int latestFilter = FILTER_BY_PRODUCTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Mes cultures");

        map = new HashMap<>();
        dataset = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.crop_info_recycler);

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

        serviceIntent = new Intent(this, LocationService.class);
        resultReceiver = new ServiceResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new RequestCropList(this).execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private class RequestCropList extends AsyncTask<Void, Void, List<SimpleInterventions>> {

        Context context;
        String farm;


        RequestCropList(final Context context) {
            this.context = context;
            this.farm = MainActivity.FARM_ID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected List<SimpleInterventions> doInBackground(Void... voids) {
            AppDatabase database = AppDatabase.getInstance(context);
            return database.dao().getSimpleInterventionList(farm);
        }

        protected void onPostExecute(List<SimpleInterventions> result) {

            dataset.clear();

            if (latestFilter == FILTER_BY_PRODUCTION) {

                for (SimpleInterventions item : result) {
                    for (Crops inter : item.crops) {

                        // Get production nature from crop
                        StringBuilder sb = new StringBuilder();
                        sb.append(inter.crop.get(0).production_nature);
                        if (inter.crop.get(0).production_mode.equals("Agriculture biologique"))
                            sb.append(" bio");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(inter.crop.get(0).stop_date);
                        sb.append(" ").append(cal.get(Calendar.YEAR));

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

                    ProductionItem header = new ProductionItem();
                    header.setName(pair.getKey().toString());
                    dataset.add(header);

                    Multimap<Crop, Intervention> cropsInProd = (Multimap) pair.getValue();
                    for (Crop crop : cropsInProd.keySet()) {
                        CropItem cropItem = new CropItem();
                        cropItem.setName(crop.name);
                        cropItem.setProduction(crop.production_nature);
                        cropItem.setSurface(crop.surface_area);
                        cropItem.setStartDate(crop.start_date);
                        cropItem.setStopDate(crop.stop_date);
                        cropItem.setYield(crop.provisional_yield);
                        cropItem.setInterventions((List) cropsInProd.get(crop));
                        cropItem.setUUID(crop.uuid);
                        dataset.add(cropItem);
                    }
                    it.remove();
                }
            }

//            else {
//
//                for (SimpleInterventions item : result) {
//                    for (Crops inter : item.crops) {
//
//                        TurfMeasurement.distance(currentLocation, inter.crop.get(0).shape, TurfConstants.UNIT_METERS);
//
//                    }
//                }
//
//            }


            adapter.notifyDataSetChanged();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.crop_activity, menu);
//        if (latestFilter == FILTER_BY_PRODUCTION) {
//            menu.findItem(R.id.filter_by_production).setEnabled(false);
//            menu.findItem(R.id.filter_by_proximity).setEnabled(true);
//        } else {
//            menu.findItem(R.id.filter_by_production).setEnabled(true);
//            menu.findItem(R.id.filter_by_proximity).setEnabled(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_by_production:
                latestFilter = FILTER_BY_PRODUCTION;
                new RequestCropList(this).execute();
                this.invalidateOptionsMenu();
                return true;

            case R.id.filter_by_proximity:
                latestFilter = FILTER_BY_PROXIMITY;

                if (ContextCompat.checkSelfPermission(InfoActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(InfoActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {

                    // Request one location
                    Intent intent = new Intent(getBaseContext(), OneShotLocationIntentService.class);
                    intent.setAction(OneShotLocationIntentService.SINGLE_UPDATE);
                    intent.putExtra("receiver", resultReceiver);
                    startService(intent);

                    this.invalidateOptionsMenu();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == OneShotLocationIntentService.OK) {

            Double lat = resultData.getDouble("latitude");
            Double lon = resultData.getDouble("longitude");
            currentLocation = new Location(LocationManager.GPS_PROVIDER);
            currentLocation.setLatitude(lat);
            currentLocation.setLongitude(lon);
            new RequestCropList(this).execute();

        }


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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    new RequestCropList(this).execute();
                else
                    // Snackbar.make(mainLayout, "Permission denied", Snackbar.LENGTH_LONG).show();
                break;
        }
    }
}
