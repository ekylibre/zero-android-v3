package com.ekylibre.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.exception.ApolloException;
import com.ekylibre.android.network.EkylibreAPI;
import com.ekylibre.android.network.GraphQLClient;
import com.ekylibre.android.network.ServiceGenerator;
import com.ekylibre.android.network.pojos.AccessToken;

import java.util.Objects;

import retrofit2.Call;

import com.apollographql.apollo.api.Response;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import javax.annotation.Nonnull;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    public static final String OAUTH_URL = "https://ekylibre-test.com";
    public static final String OAUTH_GRANT_TYPE = "password";
    public static final String OAUTH_CLIENT_ID = "e31cb2014a4604ff169b08ab171715a290bd2b24f7b0409a8cc428c75a2f3b75";
    public static final String OAUTH_CLIENT_SECRET = "aa33d1f551329fc8504e2d860767428d500418132c64f746afcc9fc160476293";
    public static final String OAUTH_SCOPE = "public read:profile read:lexicon read:plots read:crops read:interventions write:interventions read:equipment write:equipment read:articles write:articles read:person write:person";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;
    private AccessToken accessToken = null;

    private Context context;
    private SharedPreferences sharedPreferences;

    // UI references.
    private TextInputLayout emailView, passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("is_authenticated", false)) {
            Log.e(TAG, "Already authenticated. Redirect to MainActivity...");
            startApp();

        } else {

            setContentView(R.layout.activity_login);

            context = this;

            // Set up the login form.
            emailView = findViewById(R.id.email);
            passwordView = findViewById(R.id.password);

            Button signInButton = findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(view -> attemptLogin());

//        passwordView.setOnEditorActionListener((textView, id, keyEvent) -> {
//            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                attemptLogin();
//                return true;
//            }
//            return false;
//        });

        }
    }

    private void startApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (authTask != null) {
            return;
        }

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = Objects.requireNonNull(emailView.getEditText()).getText().toString();
        String password = Objects.requireNonNull(passwordView.getEditText()).getText().toString();

        authTask = new UserLoginTask(email, password);
        authTask.execute((Void) null);

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String email;
        private final String password;

        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Prevent SSL HandShake failure
            fixHandShakeFailed();

            EkylibreAPI ekylibreAPI = ServiceGenerator.createService(EkylibreAPI.class);

            Call<AccessToken> call = ekylibreAPI.getNewAccessToken(OAUTH_CLIENT_ID,
                    OAUTH_CLIENT_SECRET, OAUTH_GRANT_TYPE, email, password, OAUTH_SCOPE);

            call.enqueue(new retrofit2.Callback<AccessToken>() {
                @Override
                public void onResponse(@NonNull Call<AccessToken> call, @NonNull retrofit2.Response<AccessToken> response) {
                    if (response.isSuccessful()) {

                        accessToken = response.body();
                        Log.e(TAG, "AccessToken --> " + (accessToken != null ? accessToken.getAccess_token() : null));

                        //fixHandShakeFailed();

                        ApolloClient apolloClient = GraphQLClient.getApolloClient(accessToken.getAccess_token());
                        ProfileQuery profileQuery = ProfileQuery.builder().build();
                        ApolloCall<ProfileQuery.Data> profileCall = apolloClient.query(profileQuery);

                        profileCall.enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
                            @Override
                            public void onResponse(@Nonnull Response<ProfileQuery.Data> response) {

                                // We got an access_token
                                ProfileQuery.Data data = response.data();

                                if (data != null && data.profile != null) {
                                    //Log.e(TAG, data.profile.farm);

                                    // Get shared preferences and set profile parameters
//                                    SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("firstName", data.profile.firstName);
                                    editor.putString("lastName", data.profile.lastName);
                                    editor.putString("email", email);
                                    editor.putString("access_token", accessToken.getAccess_token());
                                    editor.putString("refresh_token", accessToken.getRefresh_token());
                                    editor.putInt("token_created_at", accessToken.getCreated_at());
                                    editor.putBoolean("is_authenticated", true);
                                    editor.putString("current-farm-name", data.farms().get(0).label);
                                    editor.putString("current-farm-id", data.farms().get(0).id);
                                    editor.apply();

                                    // Finish th login activity
                                    startApp();
                                    finish();

                                } else {
                                    Log.e(TAG, "Erreur d'authentification");
                                }
                            }

                            @Override
                            public void onFailure(@Nonnull ApolloException e) {
                                Log.e(TAG, "ApolloException --> " + e.getMessage());
                                // TODO display toast error connection
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

                }
            });

            // TODO: register the new account here.
            return true;
        }

//        @Override
//        protected void onPostExecute(final Boolean success) {
//            authTask = null;
//
//            if (success) {
//                //finish();
//            } else {
//                passwordView.setError(getString(R.string.error_incorrect_password));
//                passwordView.requestFocus();
//            }
//        }

        @Override
        protected void onCancelled() {
            authTask = null;
        }

    }

    void fixHandShakeFailed() {
        try {
            Log.e(TAG, "Updating Security Policy");
            ProviderInstaller.installIfNeeded(this);

        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play services is out of date, disabled, etc.
            Log.e(TAG, "GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
            Log.e(TAG, "GooglePlayServicesNotAvailableException");
        }
        Log.e(TAG, "fixHandShake done");
    }
}

