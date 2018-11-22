package com.ekylibre.android.network.helpers;


import androidx.annotation.NonNull;
import timber.log.Timber;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;
import com.google.gson.JsonArray;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

            String[] polygons = string.substring(3, string.length()-3).split("]],\\[\\[");
            // Use only outer Polygon
            String[] points = polygons[0].split("],\\[");

            List<Point> lngLats = new ArrayList<>();
            for (String point : points) {
                String[] lngLat = point.split(",");
                lngLats.add(Point.fromLngLat(Double.parseDouble(lngLat[0]), Double.parseDouble(lngLat[1])));
            }
            return Polygon.fromLngLats(Collections.singletonList(lngLats));


            // JSON test TODO try to make this working later and accept holes

//            JSONObject json = new JSONObject();
//            try {
//                JSONObject array = new JSONObject(value.toString());
//                Timber.e(array.toString());
//                json.put("coordinates", array);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            Timber.e(json.toString());
//            return Polygon.fromJson(json.toString());


            // Old way
//
//            String string = value.value.toString();
//            String[] points = string.substring(3, string.length()-3).split("],\\[");
//            List<Point> lngLats = new ArrayList<>();
//            for (String point : points) {
//                String[] lngLat = point.split(",");
//                lngLats.add(Point.fromLngLat(Double.parseDouble(lngLat[0]), Double.parseDouble(lngLat[1])));
//            }
//            return Polygon.fromLngLats(Collections.singletonList(lngLats));

        }

        @NonNull
        @Override
        public CustomTypeValue encode(@NonNull Polygon value) {

            List<List<Point>> coordinates = value.coordinates();
            StringBuilder sb = new StringBuilder();
            sb.append("[");
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
            sb.append("]");
            return CustomTypeValue.fromRawValue(sb.toString());
        }
    };


    public static CustomTypeAdapter<Point> customPointAdapter = new CustomTypeAdapter<Point>() {

        @Override
        public Point decode(CustomTypeValue value) {
            String string = value.value.toString();
            String[] lngLat = string.substring(1, string.length()-1).split(",");
            return Point.fromLngLat(Double.parseDouble(lngLat[0]), Double.parseDouble(lngLat[1]));
        }

        @NonNull
        @Override
        public CustomTypeValue encode(@NonNull Point value) {
            String point = "[" + value.latitude() + "," + value.longitude() + "]";
            return CustomTypeValue.fromRawValue(point);
        }
    };
}