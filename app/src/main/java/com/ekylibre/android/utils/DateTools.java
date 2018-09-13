package com.ekylibre.android.utils;


import com.ekylibre.android.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTools {

    private static SimpleDateFormat SIMPLE_DATE = new SimpleDateFormat("d MMM", MainActivity.LOCALE);
    public static SimpleDateFormat STANDARD_DISPLAY = new SimpleDateFormat("dd/MM/yyyy", MainActivity.LOCALE);
    private static long TODAY = setMidnight();
    private static int DAY = 86400000;

    public static String display(Date date) {

        if (date.getTime() == TODAY)
            return "aujourd'hui";

        else if (date.getTime() == TODAY - DAY)
            return "hier";

        return SIMPLE_DATE.format(date);
    }

    private static long setMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
