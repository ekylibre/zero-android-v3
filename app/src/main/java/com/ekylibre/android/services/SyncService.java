package com.ekylibre.android.services;


import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.relations.InterventionCrop;

import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;
import com.ekylibre.android.network.GraphQLClient;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;


import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getName();

    public static final int DONE = 10;

    public static final String ACTION_FIRST_TIME_SYNC = "com.ekylibre.android.services.action.FIRST_TIME_SYNC";
    public static final String ACTION_VERIFY_TOKEN = "com.ekylibre.android.services.action.VERIFY_TOKEN";

//    // TODO: Rename parameters
//    private static final String EXTRA_PARAM1 = "com.ekylibre.android.services.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "com.ekylibre.android.services.extra.PARAM2";

    private static String ACCESS_TOKEN;
    private static SharedPreferences sharedPreferences;
    private static AppDatabase database;


    public SyncService() {
        super("SyncService");
    }

//    public static void startActionVerifyToken(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, SyncService.class);
//        intent.setAction(ACTION_VERIFY_TOKEN);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        intent.putExtra("receiverTag", new SyncResultReceiver(new Handler()));
//        context.startService(intent);
//    }

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

        final ResultReceiver receiver = intent.getParcelableExtra("receiver");

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        ACCESS_TOKEN = sharedPreferences.getString("access_token", null);

        final String action = intent.getAction();

        if (ACTION_FIRST_TIME_SYNC.equals(action))
            handleActionFirstTimeSync(receiver);

        else if (ACTION_VERIFY_TOKEN.equals(action))
            handleActionVerifyToken();

    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFirstTimeSync(ResultReceiver receiver) {

        Log.e(TAG, "FirstTimeSync...");

        Context context = this;

        database = AppDatabase.getInstance(this);

        List<Integer> interventionEkyIdList = database.dao().interventionsEkiIdList();
        //List<Integer> phytosEkyIdList = database.dao().phytosEkiIdList();

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        apolloClient.query(PullQuery.builder().build())
                .enqueue(new ApolloCall.Callback<PullQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<PullQuery.Data> response) {

                PullQuery.Data data = response.data();

                if (data != null && data.farms() != null) {

                    Log.e(TAG, "Nombre de fermes: " + data.farms().size());

                    // TODO: Farms selector
                    // Saving latest farm (only one for now)
                    PullQuery.Farm farm = data.farms().get(0);
                    Farm newFarm = new Farm(farm.id(), farm.label());
                    database.dao().insert(newFarm);

                    // Saving current farm in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current-farm-id", farm.id());
                    editor.putString("current-farm-name", farm.label());
                    editor.apply();

                    Log.e(TAG, "Fetching crops...");

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
                        Log.e(TAG, "Fetching articles...");
                        for (PullQuery.Article article : farm.articles()) {

                            if (article.type().equals("chemical")) {
                                if (database.dao().setPhytoEkyId(Integer.valueOf(article.id()), article.referenceId()) != 1) {
                                    Phyto newPhyto = new Phyto(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                            null, article.referenceId(), null,
                                            null, null, false, true, "LITER");
                                    database.dao().insert(newPhyto);
                                }
                            }

                            if (article.type().equals("seed")) {
                                if (database.dao().setSeedEkyId(Integer.valueOf(article.id()), article.referenceId()) != 1) {
                                    Seed newSeed = new Seed(Integer.valueOf(article.referenceId()), Integer.valueOf(article.id()), article.name(),
                                            null, false, true, "KILOGRAM");
                                    database.dao().insert(newSeed);
                                }
                            }
                        }
                    }

                    // Processing interventions
                    if (!farm.interventions().isEmpty()) {
                        int index = 0;
                        for (PullQuery.Intervention inter : farm.interventions()) {

                            if (!interventionEkyIdList.contains(Integer.valueOf(inter.id()))) {
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
                                for (PullQuery.Input input : farm.interventions().get(index).inputs()) {

                                    // Phytosanitary products
                                    if (input.nature().equals("chemical")) {
                                        Integer phytoId = database.dao().getPhytoId(Integer.valueOf(input.articleId()));
                                        InterventionPhytosanitary interventionPhyto =
                                                new InterventionPhytosanitary(input.quantityValue(), input.unit().toString(), newInterId, phytoId);
                                        database.dao().insert(interventionPhyto);
                                    }

                                    // Seeds
                                    if (input.nature().equals("seed")) {
                                        Integer seedId = database.dao().getSeedId(Integer.valueOf(input.articleId()));
                                        InterventionSeed interventionSeed =
                                                new InterventionSeed(input.quantityValue(), input.unit().toString(), newInterId, seedId);
                                        database.dao().insert(interventionSeed);
                                    }

                                }
                            }
                            ++index;
                        }
                    }
                    // TODO: update recyclerView MainActivity (broadcast)
                    MainActivity.lastSyncTime = new Date();
                    receiver.send(DONE, new Bundle());
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
    private void handleActionVerifyToken() {

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
