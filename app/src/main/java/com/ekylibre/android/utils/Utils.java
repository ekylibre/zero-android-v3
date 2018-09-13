package com.ekylibre.android.utils;

import java.text.DecimalFormat;

/**
 * Created by Rémi de Chazelles on 04/09/18.
 */
public class Utils {

    public static DecimalFormat decimalFormat = new DecimalFormat("0.#");

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void setupUI(View view, Activity activity) {
//
//        // Set up touch listener for non-text box views to hide keyboard.
//        if (!(view instanceof EditText)) {
//            view.setOnTouchListener((v, event) -> {
//                hideSoftKeyboard(activity);
//                v.performClick();
//                return false;
//            });
//        }
//
//        //If a layout container, iterate over children and seed recursion.
//        if (view instanceof ViewGroup) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                View innerView = ((ViewGroup) view).getChildAt(i);
//                setupUI(innerView, activity);
//            }
//        }
//    }
//
//    private static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager =
//                (InputMethodManager) activity.getSystemService(
//                        Activity.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) {
//            inputMethodManager.hideSoftInputFromWindow(
//                    Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);
//        }
//    }
}
