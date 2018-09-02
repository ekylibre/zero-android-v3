package com.ekylibre.android.utils;

import android.arch.persistence.room.TypeConverter;

import com.ekylibre.android.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Converters {

    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat( "yyyy-MM-dd", MainActivity.LOCALE);

    @TypeConverter
    public static Date toDate(String value) {
        try {
            return value == null ? null : ISO8601.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(value + " is not a valid ISO 8601 date", e );
        }
    }

    @TypeConverter
    public static String toString(Date date) {
        return date == null ? null : ISO8601.format(date);
    }

}
