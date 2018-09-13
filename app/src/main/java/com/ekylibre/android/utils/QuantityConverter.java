package com.ekylibre.android.utils;


import com.ekylibre.android.InterventionActivity;
import static com.ekylibre.android.utils.Utils.decimalFormat;


public class QuantityConverter {

    public static String getText(float quantity, Unit unit) {

        float surface = InterventionActivity.surface;
        float total;
        String message;

        // Return area_quantity in case of quantity and quantity in case of area quantity
        if (unit.surface_factor == 0) {
            total = quantity / surface;
            message = String.format("Soit %s %s par hectare", decimalFormat.format(total), unit.name);
        }
        else {
            total = quantity * (surface * unit.surface_factor);
            message = String.format("Soit %s %s", decimalFormat.format(total), unit.quantity_name_only);
        }

        return message;
    }
}






