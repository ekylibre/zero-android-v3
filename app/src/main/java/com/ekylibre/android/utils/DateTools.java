package com.ekylibre.android.utils;

import android.util.Log;

import com.ekylibre.android.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.abs;


public class DateTools {

    private static final String TAG = "DateTools";

    private static SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat("d MMM", MainActivity.LOCALE);
    private static SimpleDateFormat SIMPLE_DATE_YEAR = new SimpleDateFormat("d MMM yyyy", MainActivity.LOCALE);
    private static long TODAY = setMidnight();

    public static String display(Date date) {

        int diffSec = (int) (date.getTime() - TODAY) / 1000;
        int daySec = 86400;
        int yearSec = daySec * 365;

        if (diffSec > 0 && diffSec < daySec) {
            return "aujourd'hui";
        }
        else if (abs(diffSec) < daySec && diffSec < 0) {
            return "hier";
        }
        else if (abs(diffSec) < yearSec) {
            return SIMPLE_DATE.format(date);
        }
        return SIMPLE_DATE_YEAR.format(date);
    }

    private static long setMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
