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

import com.ekylibre.android.FarmsQuery;
import com.ekylibre.android.ProfileQuery;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.network.GraphQLClient;
import com.ekylibre.android.utils.Converters;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

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

        ApolloClient apolloClient = GraphQLClient.getApolloClient(ACCESS_TOKEN);

        apolloClient.query(FarmsQuery
                .builder()
                .build()
        ).enqueue(new ApolloCall.Callback<FarmsQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<FarmsQuery.Data> response) {

                Log.e(TAG, "OnResponse FarmCall");

                FarmsQuery.Data data = response.data();

                if (data != null && data.farms() != null) {

                    Log.e(TAG, "Nombre de fermes: " + data.farms().size());

                    FarmsQuery.Farm currentFarm = data.farms().get(data.farms().size()-1);
                    Farm farm = new Farm(currentFarm.id(), currentFarm.name());
                    database.dao().insert(farm);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("current-farm-id", currentFarm.id());
                    editor.putString("current-farm-name", currentFarm.name());
                    editor.apply();

                    Log.e(TAG, currentFarm.name());

                    for (FarmsQuery.Crop crop : Objects.requireNonNull(currentFarm.crops())) {

                        Crop newCrop = new Crop(
                                crop.uuid(), crop.name(), crop.specie(), crop.productionNature(),
                                crop.productionMode(),null,null,null,
                                Float.valueOf(crop.surfaceArea().split(" ")[0]), null,
                                crop.startDate(), crop.stopDate(), crop.plot().uuid(),
                                null, currentFarm.id());

                        database.dao().insert(newCrop);

                        Plot newPlot = new Plot(crop.plot().uuid(), crop.plot().name(), null,
                                Float.valueOf(crop.plot().surfaceArea().split(" ")[0]), null, null, null, currentFarm.id());

                        database.dao().insert(newPlot);
                    }
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
