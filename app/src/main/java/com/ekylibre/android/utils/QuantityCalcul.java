package com.ekylibre.android.utils;

import android.annotation.SuppressLint;

import com.ekylibre.android.MainActivity;

public class QuantityCalcul {

    // Mass units
    private static final String GRAM = "gram";
    private static final String KILOGRAM = "kilogram";
    private static final String QUINTAL = "quintal";
    private static final String TON = "ton";
    private static final String GRAM_PER_HECTAR = "gram_per_hectare";
    private static final String KILOGRAM_PER_HECTARE = "kilogram_per_hectare";
    private static final String QUINTAL_PER_HECTARE = "quintal_per_hectare";
    private static final String TON_PER_HECTARE = "ton_per_hectare";
    private static final String GRAM_PER_SQUARE_METER = "gram_per_square_meter";
    private static final String KILOGRAM_PER_SQUARE_METER = "kilogram_per_square_meter";
    private static final String QUINTAL_PER_SQUARE_METER = "quintal_per_square_meter";
    private static final String TON_PER_SQUARE_METER = "ton_per_square_meter";

    // Volume units
    private static final String LITER = "liter";
    private static final String HECTOLITER = "hectoliter";
    private static final String CUBIC_METER = "cubic_meter";
    private static final String LITER_PER_HECTARE = "liter_per_hectare";
    private static final String HECTOLITER_PER_HECTARE = "hectoliter_per_hectare";
    private static final String CUBIC_METER_PER_HECTARE = "cubic_meter_per_hectare";
    private static final String LITER_PER_SQUARE_METER = "liter_per_square_meter";
    private static final String HECTOLITER_PER_SQUARE_METER = "hectoliter_per_square_meter";
    private static final String CUBIC_METER_PER_SQUARE_METER = "cubic_meter_per_square_meter";


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
        return String.format(MainActivity.LOCALE,"%.1f %s au total", result, unity);
    }
}












