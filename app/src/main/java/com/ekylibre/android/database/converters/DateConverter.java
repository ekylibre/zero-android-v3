/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ekylibre.android.database.converters;

import androidx.room.TypeConverter;

import com.ekylibre.android.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateConverter {

    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat( "yyyy-MM-dd", Locale.FRANCE);

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
