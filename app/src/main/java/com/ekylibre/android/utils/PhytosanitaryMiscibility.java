package com.ekylibre.android.utils;

import android.util.Log;

import com.ekylibre.android.InterventionActivity;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.pojos.Phytos;

import java.util.ArrayList;
import java.util.List;

public class PhytosanitaryMiscibility {

    private static boolean isValid(int firstProduct, int secondProduct) {

        if (secondProduct == 5)
            return false;

        switch (firstProduct) {

            case 4:
                return secondProduct != 4;

            case 3:
                return secondProduct != 3;

            case 2:
                return secondProduct != 2;

            case 1:
                return true;

            default:
                return false;
        }
    }

//    public static boolean mixIsAuthorized(int secondProduct) {
//
//        int index = 0;
//        for (Object input : InterventionActivity.inputList) {
//
//            if (input instanceof Phytos) {
//
//                Phyto phyt = ((Phytos) input).phyto.get(0);
//                if (phyt != null) {
//                    if (!PhytosanitaryMiscibility.isValid(phyt.mix_category_code, secondProduct))
//                        ++index;
//                }
//            }
//        }
//
//        return index <= 0;
//    }

    public static boolean mixIsAuthorized(List<Integer> codes) {

        Log.e("Miscibility", codes.toString());

        for (int i = 0; i <= codes.size() - 1; i++) {
            List<Integer> codesCopy = new ArrayList<>(codes);
            codesCopy.remove(i);
            int firstProduct = codes.get(i);
            for (int secondProduct : codesCopy) {
                if (!isValid(firstProduct, secondProduct))
                    return false;
            }
        }
        return true;
    }
}
