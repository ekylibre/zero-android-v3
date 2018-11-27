package com.ekylibre.android;

import android.content.Context;
import android.os.Handler;
import android.os.ResultReceiver;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.network.EkylibreAPI;
import com.ekylibre.android.network.ServiceGenerator;
import com.ekylibre.android.network.pojos.AccessToken;
import com.ekylibre.android.services.ServiceResultReceiver;
import com.ekylibre.android.services.SyncService;
import com.ekylibre.android.utils.App;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;


import androidx.test.core.app.ApplicationProvider;
import retrofit2.Call;

import static org.junit.Assert.assertNotNull;

public class APITest {

    private static Context context;
    private static AppDatabase db;
    private static AccessToken accessToken;

    @BeforeClass
    public static void init() throws IOException,
            GooglePlayServicesRepairableException,
            GooglePlayServicesNotAvailableException {

        // Get context and creates sharedPrefs
        context = ApplicationProvider.getApplicationContext();

        // Create in memory database with initial data
        db = AppDatabase.getInMemoryInstance(context);
        db.populateInitialData(context);

        // Fix HandShake error
        ProviderInstaller.installIfNeeded(context);

        // Get access token
        EkylibreAPI ekylibreAPI = ServiceGenerator.createService(EkylibreAPI.class);
        retrofit2.Response<AccessToken> response = ekylibreAPI.getNewAccessToken(
                BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET, App.OAUTH_GRANT_TYPE,
                BuildConfig.TEST_LOGIN, BuildConfig.TEST_PASSWORD, App.OAUTH_SCOPE).execute();

        assertNotNull(response);
        accessToken = response.body();
    }

    @AfterClass
    public static void closeDb() {
        db.close();
    }

    @Test
    public void getFarmTest() {

        assertNotNull(accessToken);

//        SyncService.getFarm();


        //
//        new PerformSyncWithFreshToken(context, ACTION_SYNC_ALL, resultReceiver).execute();

//        ApolloClient apolloClient = GraphQLClient.getApolloClient(accessToken.getAccess_token());
//        FarmQuery farmQuery = FarmQuery.builder().build();
//        ApolloCall<FarmQuery.Data> farmCall = apolloClient.query(farmQuery);
//
//        farmCall.enqueue(new ApolloCall.Callback<FarmQuery.Data>() {
//
//            @Override
//            public void onResponse(@Nonnull Response<FarmQuery.Data> response) {
//                FarmQuery.Data data = response.data();
//                assertNotNull(data);
//                assertNotNull(data.farms.get(0));
//                editor.putString("current-farm-name", data.farms().get(0).label);
//                editor.putString("current-farm-id", data.farms().get(0).id);
//                editor.apply();
//            }
//
//            @Override
//            public void onFailure(@Nonnull ApolloException e) {
//                fail(e.getMessage());
//            }
//        });

    }
}
