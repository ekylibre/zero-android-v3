package com.ekylibre.android.database.converters;

import android.arch.persistence.room.TypeConverter;

import com.mapbox.geojson.Point;

/**
 * Created by Rémi de Chazelles on 09/07/18.
 */
public class PointConverter {

    @TypeConverter
    public static Point toPoint(String string) {
        if (string != null)
            return Point.fromJson(string);
        return null;
    }

    @TypeConverter
    public static String fromPoint(Point point) {
        if (point != null) {
            return point.toJson();
        }
        return null;
    }
}
