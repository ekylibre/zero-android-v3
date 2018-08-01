package com.ekylibre.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class App {

    public static final String OAUTH_GRANT_TYPE = "password";
    public static final String OAUTH_SCOPE = "public read:profile read:lexicon read:plots read:crops read:interventions write:interventions read:equipment write:equipment read:articles write:articles read:person write:person";
    public static String OAUTH_CLIENT_ID;
    public static String OAUTH_CLIENT_SECRET;
    public static String API_URL;

    // Procedures statics
    public static final String CARE = "CARE";
    public static final String CROP_PROTECTION = "CROP_PROTECTION";
    public static final String FERTILIZATION = "FERTILIZATION";
    public static final String GROUND_WORK = "GROUND_WORK";
    public static final String HARVEST = "HARVEST";
    public static final String IMPLANTATION = "IMPLANTATION";
    public static final String IRRIGATION = "IRRIGATION";

    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean response = false;

        if (cm != null)
            response = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();

        return response;
    }

}
