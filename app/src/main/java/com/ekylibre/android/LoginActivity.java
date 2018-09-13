package com.ekylibre.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.api.Response;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.network.EkylibreAPI;
import com.ekylibre.android.network.GraphQLClient;
import com.ekylibre.android.network.ServiceGenerator;
import com.ekylibre.android.network.pojos.AccessToken;
import com.ekylibre.android.utils.App;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import timber.log.Timber;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import javax.annotation.Nonnull;


public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;
    private boolean startingApp = false;
    private AccessToken accessToken;
    private SharedPreferences sharedPreferences = null;

    // UI references.
    private TextInputLayout emailView, passwordView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.API_URL = getString(getResources().getIdentifier("api_url", "string", getPackageName()));
        App.OAUTH_CLIENT_ID = BuildConfig.CLIENT_ID;
        App.OAUTH_CLIENT_SECRET = BuildConfig.CLIENT_SECRET;

        sharedPreferences = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("is_authenticated", false)) {
            Timber.i("=========== IS AUTHENTICATED ===========");
            if (!startingApp)
                startApp();
        } else {

            //Remove title bar
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

            // Remove notification bar
            //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.activity_login);

            // Set up the login form.
            emailView = findViewById(R.id.login_email);
            passwordView = findViewById(R.id.login_password);

            ConstraintLayout layout = findViewById(R.id.login_layout);
            layout.setOnClickListener(v -> hideKeyboard());

            // Display app version on page
            AppCompatTextView appVersion = findViewById(R.id.app_version);
            appVersion.setText(BuildConfig.VERSION_NAME + String.format("%s", BuildConfig.DEBUG ? " [debug]" : "" ));

            EditText passEditText = passwordView.getEditText();
            Objects.requireNonNull(passEditText).setOnEditorActionListener((textView, id, keyEvent) -> {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    authTask = null;
                    hideKeyboard();
                    //attemptLogin();
                    return true;
                }
                return false;
            });

            Button signInButton = findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(view -> {
                authTask = null;
                hideKeyboard();
                attemptLogin();
            });
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (BuildConfig.DEBUG) Log.i(TAG, "onResume ()");
//        dialog = new ProgressDialog(this);
//        dialog.setMessage(getString(R.string.authenticating));
//    }

    private void startApp() {
        startingApp = true;
        Timber.e("=========== START MAIN ACTIVITY ===========");
        showDialog(false);
        authTask = null;
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void showDialog(boolean yes){
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.authenticating));
        }
        Timber.i("Dialog window --> %s", dialog);
        if (yes && !dialog.isShowing()) {
            dialog.show();
        } else if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Prevent SSL HandShake failure
            fixHandShakeFailed();

            EkylibreAPI ekylibreAPI = ServiceGenerator.createService(EkylibreAPI.class);

            Timber.i("Client id -> %s", BuildConfig.CLIENT_ID);
            Timber.i("Client secret -> %s", BuildConfig.CLIENT_SECRET);

            Call<AccessToken> call = ekylibreAPI.getNewAccessToken(BuildConfig.CLIENT_ID,
                    BuildConfig.CLIENT_SECRET, App.OAUTH_GRANT_TYPE, email, password, App.OAUTH_SCOPE);

            call.enqueue(new retrofit2.Callback<AccessToken>() {
                @Override
                public void onResponse(@NonNull Call<AccessToken> call, @NonNull retrofit2.Response<AccessToken> response) {

                    if (response.isSuccessful()) {
                        accessToken = response.body();

                        if (accessToken != null) {
                            Timber.i("AccessToken --> %s", accessToken.getAccess_token());

                            ApolloClient apolloClient = GraphQLClient.getApolloClient(accessToken.getAccess_token());
                            ProfileQuery profileQuery = ProfileQuery.builder().build();
                            ApolloCall<ProfileQuery.Data> profileCall = apolloClient.query(profileQuery);

                            profileCall.enqueue(new ApolloCall.Callback<ProfileQuery.Data>() {
                                @Override
                                public void onResponse(@Nonnull Response<ProfileQuery.Data> response) {

                                    // We got an access_token
                                    ProfileQuery.Data data = response.data();
                                    Timber.i(String.valueOf(data));

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
                                    editor.putString("email", email);
                                    editor.putString("access_token", accessToken.getAccess_token());
                                    editor.putString("refresh_token", accessToken.getRefresh_token());
                                    editor.putInt("token_created_at", accessToken.getCreated_at());
                                    editor.putString("current-farm-name", data.farms().get(farmPosition).label);
                                    editor.putString("current-farm-id", data.farms().get(farmPosition).id);
                                    editor.putBoolean("is_authenticated", true);
                                    editor.apply();

                                    // Finish the login activity
                                    if (!sharedPreferences.getBoolean("initial_data_loaded", false)) {
                                        runOnUiThread(changeMessage);
                                        new LoadInitialData(getBaseContext()).execute();
                                        editor.putBoolean("initial_data_loaded", true);
                                        editor.apply();
                                    } else {
                                        Timber.i("=========== INITIAL DATA ALREADY LOADED ===========");
                                        if (!startingApp)
                                            startApp();
                                    }

                                }

                                @Override
                                public void onFailure(@Nonnull ApolloException e) {
                                    Timber.i("ApolloException --> %s", e.getMessage());
                                    authTask = null;
                                    showDialog(false);
                                    Snackbar.make(findViewById(R.id.login_layout),
                                            R.string.network_failure, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            authTask = null;
                            showDialog(false);
                            Snackbar.make(findViewById(R.id.login_layout),
                                    R.string.unknown_login_failure, Snackbar.LENGTH_LONG).show();
                            cancel(true);
                        }

                    } else {
                        authTask = null;
                        showDialog(false);
                        Snackbar.make(findViewById(R.id.login_layout),
                                R.string.login_failure, Snackbar.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                    authTask = null;
                    showDialog(false);
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
            showDialog(false);
        }

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
            AppDatabase database = AppDatabase.getInstance(context);
            database.populateInitialData(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Timber.i("=========== AFTER INITIAL DATA ===========");
            if (!startingApp)
                startApp();
        }
    }

    void fixHandShakeFailed() {
        try {
            Timber.i("Updating Security Policy");
            ProviderInstaller.installIfNeeded(this);

        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play services is out of date, disabled, etc.
            Timber.i("GooglePlayServicesRepairableException");
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
            Timber.i("GooglePlayServicesNotAvailableException");
        }
        Timber.i("fixHandShake done");
    }
}

