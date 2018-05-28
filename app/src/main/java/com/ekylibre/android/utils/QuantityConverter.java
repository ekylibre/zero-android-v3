package com.ekylibre.android.utils;


import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.MainActivity;

public class QuantityConverter {

    public static String getText(float quantity, Unit unit) {

        float surface = InterventionActivity.surface;
        float total;
        String message;

        // Return area_quantity in case of quantity and quantity in case of area quantity
        if (unit.surface_factor == 0) {
            total = quantity / surface;
            message = String.format(MainActivity.LOCALE, "Soit %.1f %s par hectare", total, unit.name);
        }
        else {
            total = quantity * (surface * unit.surface_factor);
            message = String.format(MainActivity.LOCALE, "Soit %.1f %s", total, unit.quantity_name_only);
        }

        return message;
    }
}


//            switch (unit.key) {
//
//                // Mass units
//                case Units.GRAM.key:
//                    result = quantity * 0.001f; unity = "kg"; break; // kilogram is the reference

//                case KILOGRAM: result = quantity; unity = "kg"; break;
//                case QUINTAL: result = quantity * 100; unity = "kg"; break;
//                case TON: result = quantity * 1000; unity = "kg"; break;
//
//                case GRAM_PER_HECTAR: result = quantity * 0.001f * surface; unity = "kg"; break;
//                case KILOGRAM_PER_HECTARE: result = quantity * surface; unity = "kg"; break;
//                case QUINTAL_PER_HECTARE: result = quantity * 100 * surface; unity = "kg"; break;
//                case TON_PER_HECTARE: result = quantity * 1000 * surface; unity = "kg"; break;
//
//                case GRAM_PER_SQUARE_METER: result = quantity * 0.001f * surface * 10000; unity = "kg"; break;
//                case KILOGRAM_PER_SQUARE_METER: result = quantity * surface * 10000; unity = "kg"; break;
//                case QUINTAL_PER_SQUARE_METER: result = quantity * 100 * surface * 10000; unity = "kg"; break;
//                case TON_PER_SQUARE_METER: result = quantity * 1000 * surface * 10000; unity = "kg"; break;
//
//                // Volume units
//                case LITER: result = quantity; unity = "l"; break;
//                case HECTOLITER: result = quantity * 100; unity = "l"; break;
//                case CUBIC_METER: result = quantity * 1000; unity = "l"; break;
//
//                case LITER_PER_HECTARE: result = quantity * surface; unity = "l"; break;
//                case HECTOLITER_PER_HECTARE: result = quantity * 100 * surface; unity = "l"; break;
//                case CUBIC_METER_PER_HECTARE: result = quantity *1000 * surface; unity = "l"; break;
//
//                case LITER_PER_SQUARE_METER: result = quantity * surface * 10000; unity = "l"; break;
//                case HECTOLITER_PER_SQUARE_METER: result = quantity * 100 * surface * 10000; unity = "l"; break;
//                case CUBIC_METER_PER_SQUARE_METER: result = quantity * 1000 * surface * 10000; unity = "l"; break;

//            }

//        }
//        else {
//
//            switch (unitAfter) {
//
//            }

//        }



//    Map<String, Float> map = new HashMap<>();
//        map.put("gram", 0.001f);
//                map.put("kilogram", 1f);
//                map.put("quintal", 100f);
//                map.put("ton", 1000f);
//
//                Log.e(TAG, map.get("quintal").toString());









