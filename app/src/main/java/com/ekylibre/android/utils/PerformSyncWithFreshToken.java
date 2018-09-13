package com.ekylibre.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.ResultReceiver;

import com.ekylibre.android.BuildConfig;
import com.ekylibre.android.network.EkylibreAPI;
import com.ekylibre.android.network.ServiceGenerator;
import com.ekylibre.android.network.pojos.AccessToken;
import com.ekylibre.android.services.SyncService;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

import retrofit2.Call;
import timber.log.Timber;


public class PerformSyncWithFreshToken extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> context;
    private SharedPreferences prefs;
    private ResultReceiver resultReceiver;
    private String action;

    public PerformSyncWithFreshToken(Context context, String action, ResultReceiver receiver) {
        this.context = new WeakReference<>(context);
        this.prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.resultReceiver = receiver;
        this.action = action;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Manage SSL handshake errors
        try {
            ProviderInstaller.installIfNeeded(context.get());
        } catch (GooglePlayServicesRepairableException e) {
            Timber.e("GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance().showErrorNotification(context.get(), e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.e("GooglePlayServicesNotAvailableException");
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        int now = (int) new Date().getTime() / 1000;
        int tokenTime = prefs.getInt("token_created_at", 0);

        // Verify if token is up to date
        if (now - tokenTime >= 7000000) {

            AccessToken token = new AccessToken();
            token.setAccess_token(prefs.getString("access_token", null));
            token.setRefresh_token(prefs.getString("refresh_token", null));
            token.setToken_type("bearer");

            EkylibreAPI ekylibreAPI = ServiceGenerator.createService(EkylibreAPI.class, token);
            Call<AccessToken> call = ekylibreAPI.getRefreshAccessToken(
                    BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET,
                    token.getRefresh_token(), "refresh_token");

            try {
                retrofit2.Response<AccessToken> response = call.execute();
                if (response.isSuccessful()) {
                    AccessToken responseToken = response.body();
                    if (responseToken != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("access_token", responseToken.getAccess_token())
                                .putString("refresh_token", responseToken.getRefresh_token())
                                .putInt("token_created_at", responseToken.getCreated_at());
                        if (!editor.commit())
                            return responseToken.getAccess_token();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Timber.i("Token is up to date");
            return prefs.getString("access_token", null);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String accessToken) {
        super.onPostExecute(accessToken);

        if (accessToken != null) {
            Intent intent = new Intent(context.get(), SyncService.class);
            intent.setAction(action);
            intent.putExtra("accessToken", accessToken);
            intent.putExtra("receiver", resultReceiver);
            context.get().startService(intent);
        }
    }
}
