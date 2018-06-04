package com.ekylibre.android.utils;


import android.content.Context;

import com.ekylibre.android.database.models.Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SpinnerLists {

    public static List<Storage> STORAGE_LIST = new ArrayList<>();
    public static List<String> STORAGE_LIST_L10N = new ArrayList<>();

    public static List<String> OUTPUT_LIST = Arrays.asList("STRAW", "GRAIN", "SILAGE");
    public static List<String> OUTPUT_LIST_L10N = new ArrayList<>();

    public static void generate(Context context) {
        for (String outputString : SpinnerLists.OUTPUT_LIST)
            SpinnerLists.OUTPUT_LIST_L10N.add(context.getString(context.getResources().getIdentifier(outputString, "string", context.getPackageName())));
    }

    public static void generateStorages() {
        STORAGE_LIST_L10N = new ArrayList<>();
        for (Storage storage : STORAGE_LIST)
            STORAGE_LIST_L10N.add(storage.name);
    }

    public static int getIndex(String name) {
        for (Storage storage : STORAGE_LIST)
            if (storage.name.equals(name))
                return STORAGE_LIST.indexOf(storage);
        return 0;
    }

}
