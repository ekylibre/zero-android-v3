package com.agriconomie.android;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ekylibre.android.BuildConfig;
import com.ekylibre.android.MainActivity;
import com.ekylibre.android.R;
import com.ekylibre.android.network.GraphQLClient;
import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.utils.App;

import com.agriconomie.android.utils.AuthStateManager;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.ResponseTypeValues;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import timber.log.Timber;


public class AppAuthLogin extends AppCompatActivity {

    // Auth constants
    private static String CLIENT_ID;  // "1_4i9aq3clcsysos8c4k8so8kgw4g80w4okc80sowwc0w0ccscwo";
    private static String CLIENT_SECRET; // "5uc7natireo0kksc40s4gs08cow4cgs004cwsw0co4o8so0ksk";
    private static String API_AGRICO;
    private static final String SCOPE = "farm";
    private static final Uri REDIRECT_URI = Uri.parse("agrico://");
    private static final int RC_AUTH = 1024;

    // Auth
    private AuthorizationService authService;
    private AuthorizationRequest authRequest;
    private AuthStateManager authStateManager;
    private AuthState authState;
    private SharedPreferences sharedPreferences;

    // UI
    private ProgressDialog dialog;
    private Button authButton;
    private TextView authText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appauth_login);
        Timber.i("BuildConfig = %s", BuildConfig.BUILD_TYPE);

        // Define API constants
        App.API_URL = getString(getResources().getIdentifier("api_url", "string", getPackageName()));
        CLIENT_ID = BuildConfig.CLIENT_ID;
        CLIENT_SECRET = BuildConfig.CLIENT_SECRET;
        API_AGRICO = getString(getResources().getIdentifier("api_agrico", "string", getPackageName()));

        Timber.i("CLIENT_ID = %s", CLIENT_ID);
        Timber.i("CLIENT_SECRET = %s", CLIENT_SECRET);

        // Layout
        authButton = findViewById(R.id.login_button);
        authButton.setOnClickListener(view -> doAuthorization());
        authText = findViewById(R.id.refresh_token);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Instanciate SharedPreferences
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        //if (sharedPreferences.getBoolean("is_authenticated", false)) {

        if (authStateManager == null) {
            Timber.i("Get authState from sharedPrefs");
            authStateManager = AuthStateManager.getInstance(this);
            authState = authStateManager.getCurrent();
        }

        if (authState.isAuthorized()) {

            // Hide authorization button
            authButton.setVisibility(View.GONE);
            authText.setVisibility(View.VISIBLE);

            if (authState.getAccessToken() != null) {
                Timber.i("Current accessToken = %s", authState.getAccessToken());
                Timber.i("Token millis=%s, now=%s", authState.getAccessTokenExpirationTime(), System.currentTimeMillis());

                if (TimeUnit.MILLISECONDS.toMinutes(authState.getAccessTokenExpirationTime() - System.currentTimeMillis()) < 5) {
                    Timber.i("Token needs to refresh");
                    startWithFreshToken();

                } else {
                    Timber.i("Token is up to date");
                    startApp();
                }
            } else if (authState.getLastAuthorizationResponse() != null) {
                Timber.i("User is authorized, but no token here... asking for");
                performTokenRequest(authState.getLastAuthorizationResponse());
            } else {
                Timber.i("Something get wrong --> fallback to authorization");
                configureAuthService();
            }

        } else {
            // Configure OAuth service
            configureAuthService();
        }
    }

    private void getFarm(String accessToken) {

        // Fix problem with server SSL HandShake
        fixHandShakeFailed();

        ApolloClient apolloClient = GraphQLClient.getApolloClient(accessToken);
        ProfileQuery profileQuery = ProfileQuery.builder().build();
        ApolloCall<ProfileQuery.Data> profileCall = apolloClient.query(profileQuery);

        profileCall.enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ProfileQuery.Data> response) {

                ProfileQuery.Data data = response.data();

                if (data != null) {

                    List<String> farmNameList = new ArrayList<>();

                    for (ProfileQuery.Farm farm : data.farms)
                        farmNameList.add(farm.label);

                    int farmPosition = 0;

                    for (String str1 : farmNameList) {
                        for (String str2 : farmNameList)
                            if (str1.compareToIgnoreCase(str2) < 0)
                                farmPosition = farmNameList.indexOf(str1);
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("firstName", data.profile.firstName);
                    editor.putString("lastName", data.profile.lastName);
                    editor.putString("current-farm-name", data.farms().get(farmPosition).label);
                    editor.putString("current-farm-id", data.farms().get(farmPosition).id);
                    editor.apply();

                    // Finish the login activity
                    if (!sharedPreferences.getBoolean("initial_data_loaded", false)) {
                        runOnUiThread(changeMessage);
                        new LoadInitialData(getApplicationContext()).execute();
                    } else {
                        Timber.e("=========== INITIAL DATA ALREADY LOADED ===========");
                        startApp();
                    }

                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Timber.e("ApolloException --> %s", e.getMessage());
                showDialog(false);
                Snackbar.make(findViewById(R.id.appauth_login_layout),
                        R.string.network_failure, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void configureAuthService() {

        authButton.setVisibility(View.VISIBLE);
        authText.setVisibility(View.GONE);

        @SuppressLint("AuthLeak")
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(API_AGRICO + "/oauth/v2/auth") /* auth endpoint */,
                Uri.parse(API_AGRICO + "/oauth/v2/token") /* token endpoint */
        );

        authState = new AuthState(serviceConfig);

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfig,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                REDIRECT_URI
        );

        authRequest = builder
                .setScope(SCOPE)
                .build();

        authStateManager = AuthStateManager.getInstance(this);
        //authState = authStateManager.getCurrent();
    }

    private void doAuthorization() {

        Timber.i("1 --> Ask for authorization");

        Timber.i("Credentials CLIENT_ID=%s, CLIENT_SECRET=%s", CLIENT_ID, CLIENT_SECRET);

        authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, RC_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_AUTH) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
            AuthorizationException exception = AuthorizationException.fromIntent(data);
            if (response != null) {
                Timber.i("1 --> Authorization passed");
                authState = authStateManager.updateAfterAuthorization(response, exception);
                sharedPreferences.edit().putBoolean("is_authenticated", true).apply();

                // Now ask for a Token
                showDialog(true);
                performTokenRequest(response);
            } else {
                Timber.e("1 --> Authorization failed !");
                showDialog(false);
            }
        } else {
            // TODO: message création compte + instance
            Timber.e("1 --> Authorization failed !");
            showDialog(false);
        }
    }

    private void performTokenRequest(AuthorizationResponse response) {

        Timber.i("2 --> Ask for Token");

        ClientAuthentication clientAuth = new ClientSecretBasic(CLIENT_SECRET);
        authService.performTokenRequest(
                response.createTokenExchangeRequest(),
                clientAuth,
                (resp, ex) -> {
                    if (resp != null) {
                        Timber.i("2 --> Token successfuly acquired --> %s", resp.accessToken);
                        authState = authStateManager.updateAfterTokenResponse(resp, ex);
                        consumeToken();
                    } else {
                        Timber.e("2 --> Token request failed");
                    }
                });
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            dialog.setMessage(getString(R.string.loading_initial_data));
        }
    };

    public class LoadInitialData extends AsyncTask<Void, Void, Void> {
        Context context;

        LoadInitialData(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Timber.i("=========== LOAD INITIAL DATA ===========");
            AppDatabase database = AppDatabase.getInstance(context);
            database.populateInitialData(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sharedPreferences.edit().putBoolean("initial_data_loaded", true).apply();
            startApp();
        }
    }

    private void showDialog(boolean yes) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.authenticating));
        }
        if (yes)
            dialog.show();
        else if (dialog.isShowing())
            dialog.dismiss();
    }

    private void consumeToken() {
        AuthState.AuthStateAction action = (accessToken, idToken, ex) -> getFarm(accessToken);
        ClientAuthentication clientAuth = new ClientSecretPost(CLIENT_SECRET);
        authState.performActionWithFreshTokens(
                authService,
                clientAuth,
                action);
    }

    private void startApp() {
        if (authService != null) {
            authService.dispose();
            authService = null;
        }
        showDialog(false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void startWithFreshToken() {
        if (authService == null)
            authService = new AuthorizationService(this);

        AuthState.AuthStateAction action = (accessToken, idToken, ex) -> {
            if (authState.getLastTokenResponse() != null || authState.getAuthorizationException() != null) {
                authState = authStateManager.updateAfterTokenResponse(authState.getLastTokenResponse(), authState.getAuthorizationException());
                Timber.i(ex, "New token = %s", accessToken);
                startApp();
            } else {
                configureAuthService();
            }
        };

        ClientAuthentication clientAuth = new ClientSecretPost(CLIENT_SECRET);
        authState.performActionWithFreshTokens(
                authService,
                clientAuth,
                action);
    }

    void fixHandShakeFailed() {
        try {
            Timber.e("Updating Security Policy");
            ProviderInstaller.installIfNeeded(this);

        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play services is out of date, disabled, etc.
            Timber.e("GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
            Timber.e("GooglePlayServicesNotAvailableException");
        }
        Timber.e("fixHandShake done");
    }
}

