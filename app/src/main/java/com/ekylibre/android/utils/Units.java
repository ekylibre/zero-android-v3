package com.ekylibre.android.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Units {

    private static final float HECTARE_FACTOR = 1;
    private static final float SQUARE_METER_FACTOR = .00001f;

    private static final float LITER_FACTOR = 1;
    private static final float HECTOLITER_FACTOR = .001f;
    private static final float CUBIC_METER_FACTOR = .0001f;

    private static final float GRAM_FACTOR = .0001f;
    private static final float KILOGRAM_FACTOR = 1;
    private static final float QUINTAL_FACTOR = 100;
    private static final float TON_FACTOR = 1000;

    // Volume (reference is LITER_PER_HECTARE)
    public static final Unit LITER = new Unit("LITER", LITER_FACTOR);
    public static final Unit LITER_PER_HECTARE = new Unit("LITER_PER_HECTARE", "LITER", LITER_FACTOR, HECTARE_FACTOR);
    public static final Unit LITER_PER_SQUARE_METER = new Unit("LITER_PER_SQUARE_METER", "LITER", LITER_FACTOR, SQUARE_METER_FACTOR);
    public static final Unit HECTOLITER = new Unit("HECTOLITER", HECTOLITER_FACTOR);
    public static final Unit HECTOLITER_PER_HECTARE = new Unit("HECTOLITER_PER_HECTARE", "HECTOLITER", HECTOLITER_FACTOR, HECTARE_FACTOR);
    public static final Unit HECTOLITER_PER_SQUARE_METER = new Unit("HECTOLITER_PER_SQUARE_METER", "HECTOLITER", HECTOLITER_FACTOR, SQUARE_METER_FACTOR);
    public static final Unit CUBIC_METER = new Unit("CUBIC_METER", CUBIC_METER_FACTOR);
    public static final Unit CUBIC_METER_PER_HECTARE = new Unit("CUBIC_METER_PER_HECTARE", "CUBIC_METER", CUBIC_METER_FACTOR, HECTARE_FACTOR);
    public static final Unit CUBIC_METER_PER_SQUARE_METER = new Unit("CUBIC_METER_PER_SQUARE_METER", "CUBIC_METER", CUBIC_METER_FACTOR, SQUARE_METER_FACTOR);

    // Mass
    public static final Unit GRAM = new Unit("GRAM", GRAM_FACTOR);
    public static final Unit GRAM_PER_HECTARE = new Unit("GRAM_PER_HECTARE", "GRAM", GRAM_FACTOR, HECTARE_FACTOR);
    public static final Unit GRAM_PER_SQUARE_METER = new Unit("GRAM_PER_SQUARE_METER", "GRAM", GRAM_FACTOR, SQUARE_METER_FACTOR);
    public static final Unit KILOGRAM = new Unit("KILOGRAM", KILOGRAM_FACTOR);
    public static final Unit KILOGRAM_PER_HECTARE = new Unit("KILOGRAM_PER_HECTARE", "KILOGRAM", KILOGRAM_FACTOR, HECTARE_FACTOR);
    public static final Unit KILOGRAM_PER_SQUARE_METER = new Unit("KILOGRAM_PER_SQUARE_METER", "KILOGRAM", KILOGRAM_FACTOR, SQUARE_METER_FACTOR);
    public static final Unit QUINTAL = new Unit("QUINTAL", QUINTAL_FACTOR);
    public static final Unit QUINTAL_PER_HECTARE = new Unit("QUINTAL_PER_HECTARE", "QUINTAL", QUINTAL_FACTOR, HECTARE_FACTOR);
    public static final Unit QUINTAL_PER_SQUARE_METER = new Unit("QUINTAL_PER_SQUARE_METER", "QUINTAL", QUINTAL_FACTOR, SQUARE_METER_FACTOR);
    public static final Unit TON = new Unit("TON", TON_FACTOR);
    public static final Unit TON_PER_HECTARE = new Unit("TON_PER_HECTARE", "TON", TON_FACTOR, HECTARE_FACTOR);
    public static final Unit TON_PER_SQUARE_METER = new Unit("TON_PER_SQUARE_METER", "TON", TON_FACTOR, SQUARE_METER_FACTOR);

    // Unit
//    public static final Unit METER = new Unit("METER", );
//    public static final Unit UNIT = new Unit("UNIT", );
//    public static final Unit THOUSAND = new Unit("THOUSAND", );

    // Lists
    public static final List<Unit> IRRIGATION_UNITS = Arrays.asList(CUBIC_METER, LITER, HECTOLITER);
    public static final List<Unit> VOLUME_UNITS = Arrays.asList(LITER, LITER_PER_HECTARE, LITER_PER_SQUARE_METER, HECTOLITER, HECTOLITER_PER_HECTARE, HECTOLITER_PER_SQUARE_METER, CUBIC_METER, CUBIC_METER_PER_HECTARE, CUBIC_METER_PER_SQUARE_METER);
    public static final List<Unit> MASS_UNITS = Arrays.asList(GRAM, GRAM_PER_HECTARE, GRAM_PER_SQUARE_METER, KILOGRAM, KILOGRAM_PER_HECTARE, KILOGRAM_PER_SQUARE_METER, QUINTAL, QUINTAL_PER_HECTARE, QUINTAL_PER_SQUARE_METER, TON, TON_PER_HECTARE, TON_PER_SQUARE_METER);
    public static final List<Unit> ALL_UNITS = Arrays.asList(LITER, LITER_PER_HECTARE, LITER_PER_SQUARE_METER, HECTOLITER, HECTOLITER_PER_HECTARE, HECTOLITER_PER_SQUARE_METER, CUBIC_METER, CUBIC_METER_PER_HECTARE, CUBIC_METER_PER_SQUARE_METER, GRAM, GRAM_PER_HECTARE, GRAM_PER_SQUARE_METER, KILOGRAM, KILOGRAM_PER_HECTARE, KILOGRAM_PER_SQUARE_METER, QUINTAL, QUINTAL_PER_HECTARE, QUINTAL_PER_SQUARE_METER, TON, TON_PER_HECTARE, TON_PER_SQUARE_METER);

    public static final List<String> IRRIGATION_UNITS_L10N = new ArrayList<>();
    public static final List<String> VOLUME_UNITS_L10N= new ArrayList<>();
    public static final List<String> MASS_UNITS_L10N = new ArrayList<>();

    public static Unit getUnit(String name) {
        for (Unit unit : ALL_UNITS)
            if (unit.key.equals(name) || unit.name.equals(name))
                return unit;
        return null;
    }
}

//    // Volume
//    public static final String LITER = "LITER";
//    public static final String LITER_PER_HECTARE = "LITER_PER_HECTARE";
//    public static final String LITER_PER_SQUARE_METER = "LITER_PER_SQUARE_METER";
//    public static final String HECTOLITER = "HECTOLITER";
//    public static final String HECTOLITER_PER_HECTARE = "HECTOLITER_PER_HECTARE";
//    public static final String HECTOLITER_PER_SQUARE_METER = "HECTOLITER_PER_SQUARE_METER";
//    public static final String CUBIC_METER = "CUBIC_METER";
//    public static final String CUBIC_METER_PER_HECTARE = "CUBIC_METER_PER_HECTARE";
//    public static final String CUBIC_METER_PER_SQUARE_METER = "CUBIC_METER_PER_SQUARE_METER";
//
//    // Mass
//    public static final String GRAM = "GRAM";
//    public static final String GRAM_PER_HECTARE = "GRAM_PER_HECTARE";
//    public static final String GRAM_PER_SQUARE_METER = "GRAM_PER_SQUARE_METER";
//    public static final String KILOGRAM = "KILOGRAM";
//    public static final String KILOGRAM_PER_HECTARE = "KILOGRAM_PER_HECTARE";
//    public static final String KILOGRAM_PER_SQUARE_METER = "KILOGRAM_PER_SQUARE_METER";
//    public static final String QUINTAL = "QUINTAL";
//    public static final String QUINTAL_PER_HECTARE = "QUINTAL_PER_HECTARE";
//    public static final String QUINTAL_PER_SQUARE_METER = "QUINTAL_PER_SQUARE_METER";
//    public static final String TON = "TON";
//    public static final String TON_PER_HECTARE = "TON_PER_HECTARE";
//    public static final String TON_PER_SQUARE_METER = "TON_PER_SQUARE_METER";
//
//    // Unit
//    public static final String METER = "METER";
//    public static final String UNIT = "UNIT";
//    public static final String THOUSAND = "THOUSAND";
//
//    // Lists
//    public static final List<String> IRRIGATION_UNITS = Arrays.asList(CUBIC_METER, LITER, HECTOLITER);
//    public static final List<String> VOLUME_UNITS = Arrays.asList(LITER, LITER_PER_HECTARE, LITER_PER_SQUARE_METER, HECTOLITER, HECTOLITER_PER_HECTARE, HECTOLITER_PER_SQUARE_METER, CUBIC_METER, CUBIC_METER_PER_HECTARE, CUBIC_METER_PER_SQUARE_METER);
//    public static final List<String> MASS_UNITS = Arrays.asList(GRAM, GRAM_PER_HECTARE, GRAM_PER_SQUARE_METER, KILOGRAM, KILOGRAM_PER_HECTARE, KILOGRAM_PER_SQUARE_METER, QUINTAL, QUINTAL_PER_HECTARE, QUINTAL_PER_SQUARE_METER, TON, TON_PER_HECTARE, TON_PER_SQUARE_METER);
//
//    public static final List<String> IRRIGATION_UNITS_L10N = new ArrayList<>();
//    public static final List<String> VOLUME_UNITS_L10N= new ArrayList<>();
//    public static final List<String> MASS_UNITS_L10N = new ArrayList<>();
