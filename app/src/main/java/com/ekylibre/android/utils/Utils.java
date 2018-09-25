package com.ekylibre.android.utils;

import android.widget.EditText;

import com.ekylibre.android.MainActivity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Rémi de Chazelles on 04/09/18.
 */
public class Utils {

    public static final SimpleDateFormat ISO8601 = new SimpleDateFormat( "yyyy-MM-dd", MainActivity.LOCALE);
    public static DecimalFormat decimalFormat = new DecimalFormat("0.#");

    public static Float getEditTextToFloat(EditText editText) {
        String text = editText.getText().toString().replace(",", ".");
        return Float.valueOf(text);
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static Date stringToDate(String value) {
        try {
            return value == null ? null : ISO8601.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(value + " is not a valid ISO 8601 date", e );
        }
    }

    public static String dateToString(Date date) {
        return date == null ? null : ISO8601.format(date);
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
