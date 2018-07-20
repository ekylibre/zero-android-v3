package com.ekylibre.android;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.pojos.CropsByProduction;

import java.util.List;

import timber.log.Timber;

public class InfoActivity extends AppCompatActivity
        implements InfoFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Mes cultures");

        new RequestCropList(this).execute();
    }

    private class RequestCropList extends AsyncTask<Void, Void, Void> {

        Context context;

        RequestCropList(final Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppDatabase database = AppDatabase.getInstance(context);
            List<CropsByProduction> cropsByProduction = database.dao().cropsByProduction(MainActivity.FARM_ID);

            for (CropsByProduction item : cropsByProduction) {
                Timber.i("Crop: %s - Interventions: %s", item.crop.name, item.interventionCrop);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            if (editIntervention != null) {
//                int count = 0;
//                float total = 0;
//                if (BuildConfig.DEBUG) Log.e(TAG, plotList.toString());
//                for (Plots plot : plotList) {
//                    for (Crops culture : editIntervention.crops) {
//                        for (Crop crop : plot.crops) {
//                            if (culture.crop.get(0).uuid.equals(crop.uuid)) {
//                                crop.is_checked = true;
//                                plot.plot.is_checked = true;
//                                total += crop.surface_area;
//                                ++count;
//                            }
//                        }
//                    }
//                }
//                surface = total;
//                String cropCount = context.getResources().getQuantityString(R.plurals.crops, count, count);
//                cropSummaryText = String.format(MainActivity.LOCALE, "%s • %.1f ha", cropCount, total);
//                cropSummary.setText(cropSummaryText);
//                cropAddLabel.setVisibility(View.GONE);
//                cropSummary.setVisibility(View.VISIBLE);
//            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_by_production:

                return true;

            case R.id.filter_by_proximity:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Object selection) {
        if (selection != null) {
            // TODO
        }
    }
}
