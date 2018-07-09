package com.ekylibre.android.database.converters;

import android.arch.persistence.room.TypeConverter;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Rémi de Chazelles on 09/07/18.
 */
public class PolygonConverter {

    @TypeConverter
    public static Polygon toPolygon(String string) {
        if (string != null) {
            String[] points = string.substring(3, string.length()-3).split("],\\[");
            List<Point> lngLats = new ArrayList<>();
            for (String point : points) {
                String[] lngLat = point.split(",");
                lngLats.add(Point.fromLngLat(Double.parseDouble(lngLat[1]), Double.parseDouble(lngLat[0])));
            }
            return Polygon.fromLngLats(Collections.singletonList(lngLats));
        }
        return null;
    }

    @TypeConverter
    public static String fromPolygon(Polygon polygon) {
        if (polygon != null) {
            List<List<Point>> coordinates = polygon.coordinates();
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (coordinates != null) {
                int j = 0;
                for (List<Point> lngLats : coordinates) {
                    int i = 0;
                    sb.append("[");
                    for (Point point : lngLats) {
                        sb.append(String.format("[%s,%s]", point.longitude(), point.latitude()));
                        if(i++ < lngLats.size() - 1)
                            sb.append(",");
                    }
                    sb.append("]");
                    if(j++ < coordinates.size() - 1)
                        sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        return null;
    }
}
