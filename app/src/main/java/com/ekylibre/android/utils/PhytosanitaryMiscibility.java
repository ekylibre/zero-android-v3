package com.ekylibre.android.utils;

public class PhytosanitaryMiscibility {

    public static boolean isValid(int firstProduct, int secondProduct) {

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
}
