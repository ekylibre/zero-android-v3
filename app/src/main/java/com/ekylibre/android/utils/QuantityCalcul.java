package com.ekylibre.android.utils;


import android.util.Log;

import com.ekylibre.android.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuantityCalcul {

    // Mass units
    private static final String GRAM = "GRAM";
    private static final String KILOGRAM = "KILOGRAM";
    private static final String QUINTAL = "QUINTAL";
    private static final String TON = "TON";
    private static final String GRAM_PER_HECTAR = "GRAM_PER_HECTARE";
    private static final String KILOGRAM_PER_HECTARE = "KILOGRAM_PER_HECTARE";
    private static final String QUINTAL_PER_HECTARE = "QUINTAL_PER_HECTARE";
    private static final String TON_PER_HECTARE = "TON_PER_HECTARE";
    private static final String GRAM_PER_SQUARE_METER = "GRAM_PER_SQUARE_METER";
    private static final String KILOGRAM_PER_SQUARE_METER = "KILOGRAM_PER_SQUARE_METER";
    private static final String QUINTAL_PER_SQUARE_METER = "QUINTAL_PER_SQUARE_METER";
    private static final String TON_PER_SQUARE_METER = "TON_PER_SQUARE_METER";

    // Volume units
    private static final String LITER = "LITER";
    private static final String HECTOLITER = "HECTOLITER";
    private static final String CUBIC_METER = "CUBIC_METER";
    private static final String LITER_PER_HECTARE = "LITER_PER_HECTARE";
    private static final String HECTOLITER_PER_HECTARE = "HECTOLITER_PER_HECTARE";
    private static final String CUBIC_METER_PER_HECTARE = "CUBIC_METER_PER_HECTARE";
    private static final String LITER_PER_SQUARE_METER = "LITER_PER_SQUARE_METER";
    private static final String HECTOLITER_PER_SQUARE_METER = "HECTOLITER_PER_SQUARE_METER";
    private static final String CUBIC_METER_PER_SQUARE_METER = "CUBIC_METER_PER_SQUARE_METER";

    public static String getText(float quantity, String unitBefore, String unitAfter, float surface) {

        float result = 0f;
        String unity = "kg";
        
        if (unitBefore.equals(unitAfter)) {

            switch (unitAfter) {

                // Mass units
                case GRAM: result = quantity * 0.001f; unity = "kg"; break; // kilogram is the reference
                case KILOGRAM: result = quantity; unity = "kg"; break;
                case QUINTAL: result = quantity * 100; unity = "kg"; break;
                case TON: result = quantity * 1000; unity = "kg"; break;

                case GRAM_PER_HECTAR: result = quantity * 0.001f * surface; unity = "kg"; break;
                case KILOGRAM_PER_HECTARE: result = quantity * surface; unity = "kg"; break;
                case QUINTAL_PER_HECTARE: result = quantity * 100 * surface; unity = "kg"; break;
                case TON_PER_HECTARE: result = quantity * 1000 * surface; unity = "kg"; break;

                case GRAM_PER_SQUARE_METER: result = quantity * 0.001f * surface * 10000; unity = "kg"; break;
                case KILOGRAM_PER_SQUARE_METER: result = quantity * surface * 10000; unity = "kg"; break;
                case QUINTAL_PER_SQUARE_METER: result = quantity * 100 * surface * 10000; unity = "kg"; break;
                case TON_PER_SQUARE_METER: result = quantity * 1000 * surface * 10000; unity = "kg"; break;

                // Volume units
                case LITER: result = quantity; unity = "l"; break;
                case HECTOLITER: result = quantity * 100; unity = "l"; break;
                case CUBIC_METER: result = quantity * 1000; unity = "l"; break;

                case LITER_PER_HECTARE: result = quantity * surface; unity = "l"; break;
                case HECTOLITER_PER_HECTARE: result = quantity * 100 * surface; unity = "l"; break;
                case CUBIC_METER_PER_HECTARE: result = quantity *1000 * surface; unity = "l"; break;

                case LITER_PER_SQUARE_METER: result = quantity * surface * 10000; unity = "l"; break;
                case HECTOLITER_PER_SQUARE_METER: result = quantity * 100 * surface * 10000; unity = "l"; break;
                case CUBIC_METER_PER_SQUARE_METER: result = quantity * 1000 * surface * 10000; unity = "l"; break;

            }

        }
//        else {
//
//            switch (unitAfter) {
//
//            }

//        }

        return String.format(MainActivity.LOCALE,"%.1f %s au total", result, unity);
    }
}


//    Map<String, Float> map = new HashMap<>();
//        map.put("gram", 0.001f);
//                map.put("kilogram", 1f);
//                map.put("quintal", 100f);
//                map.put("ton", 1000f);
//
//                Log.e(TAG, map.get("quintal").toString());









