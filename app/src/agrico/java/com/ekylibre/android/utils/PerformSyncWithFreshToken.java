package com.ekylibre.android.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.ResultReceiver;

import com.ekylibre.android.services.SyncService;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import net.openid.appauth.AuthState;

import java.lang.ref.WeakReference;

import timber.log.Timber;


public class PerformSyncWithFreshToken extends AsyncTask<Void, Void, String> {

    private final WeakReference<Context> context;
    private ResultReceiver resultReceiver;
    private String action;

    public PerformSyncWithFreshToken(Context context, String action, ResultReceiver receiver) {
        this.context = new WeakReference<>(context);
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
            GoogleApiAvailability.getInstance().showErrorNotification(
                    context.get(), e.getConnectionStatusCode());
        } catch (GooglePlayServicesNotAvailableException e) {
            Timber.e("GooglePlayServicesNotAvailableException");
        }
    }

    @Override
    protected String doInBackground(Void... voids) {

        AuthStateManager authStateManager = AuthStateManager.getInstance(context.get());
        AuthState authState = authStateManager.getCurrent();

        if (authState.isAuthorized()) {
            if (authState.getNeedsTokenRefresh()) {
                authState.createTokenRefreshRequest();
                authState = authStateManager.updateAfterTokenResponse(
                        authState.getLastTokenResponse(),
                        authState.getAuthorizationException());
            }
            return authState.getAccessToken();
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
