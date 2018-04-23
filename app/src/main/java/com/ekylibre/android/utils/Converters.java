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

package com.ekylibre.android.utils;

import android.arch.persistence.room.TypeConverter;

import com.ekylibre.android.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class Converters {

    private static final SimpleDateFormat iso8601datetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

//    @TypeConverter
//    public static Date toDate(Long timestamp) {
//        return timestamp == null ? null : new Date(timestamp);
//    }

    @TypeConverter
    public static Date toDate(String iso8601) {
        return iso8601 == null ? null : new Date(iso8601);
    }

//    @TypeConverter
//    public static Long toTimestamp(Date date) {
//        return date == null ? null : date.getTime();
//    }

    @TypeConverter
    public static String toString(Date date) {
        SimpleDateFormat iso8601date = new SimpleDateFormat("yyyy-MM-dd");
        iso8601date.setTimeZone(TimeZone.getTimeZone("UTC"));
        return date == null ? null : iso8601date.format(date);
    }

}
