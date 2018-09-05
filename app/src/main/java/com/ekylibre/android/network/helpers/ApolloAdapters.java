package com.ekylibre.android.network.helpers;


import android.support.annotation.NonNull;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;
import com.ekylibre.android.MainActivity;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public abstract class ApolloAdapters implements CustomTypeAdapter<Date> {

    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat( "yyyy-MM-dd");

    public static CustomTypeAdapter<Date> customDateAdapter = new CustomTypeAdapter<Date>() {

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


    public static CustomTypeAdapter<Polygon> customPolygonAdapter = new CustomTypeAdapter<Polygon>() {

        @Override
        public Polygon decode(CustomTypeValue value) {
            String string = value.value.toString();
            String[] points = string.substring(3, string.length()-3).split("],\\[");
            List<Point> lngLats = new ArrayList<>();
            for (String point : points) {
                String[] lngLat = point.split(",");
                lngLats.add(Point.fromLngLat(Double.parseDouble(lngLat[1]), Double.parseDouble(lngLat[0])));
            }
            return Polygon.fromLngLats(Collections.singletonList(lngLats));
        }

        @NonNull
        @Override
        public CustomTypeValue encode(@NonNull Polygon value) {

            List<List<Point>> coordinates = value.coordinates();
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
            return CustomTypeValue.fromRawValue(sb.toString());
        }
    };


    public static CustomTypeAdapter<Point> customPointAdapter = new CustomTypeAdapter<Point>() {

        @Override
        public Point decode(CustomTypeValue value) {
            String json = value.value.toString();
            return Point.fromJson(json);
        }

        @NonNull
        @Override
        public CustomTypeValue encode(@NonNull Point value) {

            return CustomTypeValue.fromRawValue(value.toJson());
        }
    };
}