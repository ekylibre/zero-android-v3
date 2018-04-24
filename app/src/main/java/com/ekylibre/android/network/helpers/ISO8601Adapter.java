package com.ekylibre.android.network.helpers;


import android.support.annotation.NonNull;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ISO8601Adapter implements CustomTypeAdapter<Date> {

    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat( "yyyy-MM-dd");

    public static CustomTypeAdapter<Date> customTypeAdapter = new CustomTypeAdapter<Date>() {

        @Override
        public Date decode(CustomTypeValue value) {
            try {
                return (ISO8601.parse(value.value.toString()));
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(value + " is not a valid ISO 8601 date", e );
            }
        }

        @NonNull
        @Override
        public CustomTypeValue encode(@NonNull Date value) {
            return CustomTypeValue.fromRawValue(ISO8601.format(value.getTime()));
        }
    };
}