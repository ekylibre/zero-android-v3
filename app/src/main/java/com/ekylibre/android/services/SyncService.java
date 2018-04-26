package com.ekylibre.android.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import com.ekylibre.android.MainActivity;
import com.ekylibre.android.ProfileQuery;
import com.ekylibre.android.PullQuery;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;
import com.ekylibre.android.network.GraphQLClient;
import com.ekylibre.android.utils.Converters;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getName();

    private static final String ACTION_FIRST_TIME_SYNC = "com.ekylibre.android.services.action.FIRST_TIME_SYNC";
    private static final String ACTION_VERIFY_TOKEN = "com.ekylibre.android.services.action.VERIFY_TOKEN";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.ekylibre.android.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.ekylibre.android.services.extra.PARAM2";

    private static String ACCESS_TOKEN;
    private static SharedPreferences sharedPreferences;
    private static AppDatabase database;


    public SyncService() {
        super("SyncService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFirstTimeSync(Context context) {
        Log.e(TAG, "startActionFirstTimeSync");
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_FIRST_TIME_SYNC);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionVerifyToken(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_VERIFY_TOKEN);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance().showErrorNotification(this, e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, "GooglePlayServicesNotAvailableException");
        }

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ACCESS_TOKEN = sharedPreferences.getString("access_token", null);

        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_FIRST_TIME_SYNC.equals(action)) {
                handleActionFirstTimeSync();

            } else if (ACTION_VERIFY_TOKEN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionVerifyToken(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFirstTimeSync() {

        Log.e(TAG, "handleActionFirstTimeSync");

        database = AppDatabase.getInstance(this);

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();
        //List<Integer> phytosEkyIdList = database.dao().phytosEkiIdList();
        List<String> phytosMaaidList = database.dao().phytosMaaidList();

        Log.e(TAG, phytosMaaidList.toString());

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        apolloClient.query(PullQuery.builder().build())
                .enqueue(new ApolloCall.Callback<PullQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<PullQuery.Data> response) {

                Log.e(TAG, "OnResponse FarmCall");

                PullQuery.Data data = response.data();

                if (data != null && data.farms() != null) {

                    Log.e(TAG, "Nombre de fermes: " + data.farms().size());

                    // TODO: Farms selector
                    // Saving latest farm (only one for now)
                    PullQuery.Farm farm = data.farms().get(data.farms().size()-1);
                    Farm newFarm = new Farm(farm.id(), farm.label());
                    database.dao().insert(newFarm);

                    // Saving current farm in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current-farm-id", farm.id());
                    editor.putString("current-farm-name", farm.label());
                    editor.apply();

                    // Processing crops and associated plots
                    if (!farm.crops().isEmpty()) {
                        for (PullQuery.Crop crop : farm.crops()) {

                            // Symplify crop name
                            String name = crop.name().replace(crop.plot().name() + " | ", "");

                            // Saving crop
                            Crop newCrop = new Crop(
                                    crop.uuid(), name, crop.specie(), crop.productionNature(),
                                    crop.productionMode(), null, null, null,
                                    Float.valueOf(crop.surfaceArea().split(" ")[0]), null,
                                    crop.startDate(), crop.stopDate(), crop.plot().uuid(),
                                    null, farm.id());
                            database.dao().insert(newCrop);

                            // Saving plot
                            Plot newPlot = new Plot(crop.plot().uuid(), crop.plot().name(), null,
                                    Float.valueOf(crop.plot().surfaceArea().split(" ")[0]), null, null, null, farm.id());
                            database.dao().insert(newPlot);
                        }
                    }

                    // Processing articles
                    if (!farm.articles().isEmpty()) {
                        for (PullQuery.Article article : farm.articles()) {

                            if (article.type().equals("chemical")) {

                                Log.e(TAG, "referenceId " + article.referenceId());
                                if (phytosMaaidList.contains(article.referenceId())) {
                                    Log.e(TAG, "MAAID: " + article.referenceId());
                                    // Set Ekylibre article id to existing record
                                    database.dao().setPhytoEkyId(Integer.valueOf(article.id()), article.referenceId());
                                }
                                else {
                                    Integer newId = database.dao().lastPhytosanitaryId();
                                    newId = (newId != null) ? ++newId : 50000;
                                    Phyto newPhyto = new Phyto(newId, Integer.valueOf(article.id()), article.name(),
                                            null, article.referenceId(), null,
                                            null, null, false, true, null);
                                    database.dao().insert(newPhyto);
                                }
                            }
                        }
                    }

                    Log.e(TAG, "liste " + interventionEkyIdList.toString());

                    // Processing interventions
                    if (!farm.interventions().isEmpty()) {
                        int index = 0;
                        for (PullQuery.Intervention inter : farm.interventions()) {

                            if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {
                                Log.e(TAG, "eky_id " + Integer.valueOf(inter.id()));

                                // Save main intervention
                                Intervention newInter = new Intervention();
                                newInter.setEky_id(Integer.valueOf(inter.id()));
                                newInter.setType(inter.type().toString());
                                if (inter.waterQuantity() != null) {
                                    newInter.setWater_quantity((int) (long) inter.waterQuantity());
                                    newInter.setWater_unit(inter.waterUnit().toString());
                                }
                                newInter.setFarm(farm.id());
                                newInter.setStatus("sync");

                                int newInterId = (int) (long) database.dao().insert(newInter);

                                //                            for (PullQuery.Input input : farm.interventions().get(index).inputs()) {
                                //                                String type = input.id()
                                //                            }

                                // Saving WorkingDays
                                for (PullQuery.WorkingDay wd : farm.interventions().get(index).workingDays()) {
                                    InterventionWorkingDay interventionWD =
                                            new InterventionWorkingDay(newInterId, wd.executionDate(), (int) (long) wd.hourDuration());
                                    database.dao().insert(interventionWD);
                                }

                                // Saving Crops (targets)
                                for (PullQuery.Target target : farm.interventions().get(index).targets()) {
                                    InterventionCrop interventionCrop =
                                            new InterventionCrop(newInterId, target.crop().uuid(), 100);
                                    database.dao().insert(interventionCrop);
                                }

                                // Saving Inputs
//                                for (PullQuery.Input input : farm.interventions().get(index).inputs()) {
//
//                                    Log.e(TAG, "input article id --> " + input.articleId());
//
//                                    int localPhytoId = 0;
//
//                                    // Phytosanitary products
//                                    if (input.nature().equals("chemical"))
//                                        localPhytoId = database.dao().getPhytoId(Integer.valueOf(input.articleId()));
//                                        Log.e(TAG, "phyto_id = " + localPhytoId);
//                                        InterventionPhytosanitary interventionPhyto = new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(), newInterId, localPhytoId);
//                                        database.dao().insert(interventionPhyto);
//                                }
                            }
                            ++index;
                        }
                    }
                    // TODO: update recyclerView MainActivity
                    MainActivity.lastSyncTime = new Date();
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, e.getMessage(), e);

            }
        });

        //AppDatabase database = AppDatabase.getInstance(this);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionVerifyToken(String param1, String param2) {

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);
        ProfileQuery profileQuery = ProfileQuery.builder().build();
        ApolloCall<ProfileQuery.Data> profileCall = apolloClient.query(profileQuery);

        profileCall.enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<ProfileQuery.Data> response) {

                ProfileQuery.Data data = response.data();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

            }

        });

    }
}
