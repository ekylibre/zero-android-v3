package com.ekylibre.android.utils;

import android.widget.EditText;

import com.ekylibre.android.MainActivity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static Float cleanFloat(Float value) {
        return Float.valueOf(String.format(Locale.ENGLISH, "%.2f", value));
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

}
