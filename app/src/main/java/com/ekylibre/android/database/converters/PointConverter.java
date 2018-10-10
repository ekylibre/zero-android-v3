package com.ekylibre.android.database.converters;

import androidx.room.TypeConverter;

import com.mapbox.geojson.Point;

/**
 * Created by Rémi de Chazelles on 09/07/18.
 */
public class PointConverter {

    @TypeConverter
    public static Point toPoint(String string) {
        if (string != null) {
            String[] lngLat = string.substring(1, string.length()-1).split(",");
            return Point.fromLngLat(Double.parseDouble(lngLat[0]), Double.parseDouble(lngLat[1]));
        }
        return null;
    }

    @TypeConverter
    public static String fromPoint(Point point) {
        if (point != null) {
            return "[" + point.longitude() + "," + point.latitude() + "]";
        }
        return null;
    }
}
