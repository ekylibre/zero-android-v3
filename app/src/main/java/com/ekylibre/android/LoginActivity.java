package com.ekylibre.android;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.ekylibre.android.network.EkylibreAPI;
import com.ekylibre.android.network.pojos.AccessToken;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;



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

    // UI references.
    private TextInputLayout emailView, passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
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

            Log.e(TAG, "UserLoginTask");
            fixHandShakeFailed();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(OAUTH_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();

            EkylibreAPI ekylibreAPI = retrofit.create(EkylibreAPI.class);

            Call<AccessToken> call = ekylibreAPI.getNewAccessToken(OAUTH_CLIENT_ID,
                    OAUTH_CLIENT_SECRET, OAUTH_GRANT_TYPE, email, password, OAUTH_SCOPE);

            Log.e(TAG, "You are here");

            try {
                // Actually do the request
                Response<AccessToken> response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        Log.e(TAG, response.body().getAccessToken());
                }
                else {
                    Log.e(TAG, "Unsuccessful response " + response.errorBody().string());
                }

            } catch (IOException e) {
                Log.e(TAG, String.format("Call error --> %s", e.getMessage()));
            }

//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(email)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(password);
//                }
//            }



            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;

            if (success) {
                //finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

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
            GoogleApiAvailability.getInstance()
                    .showErrorNotification(this, e.getConnectionStatusCode());

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
        }
    }
}

