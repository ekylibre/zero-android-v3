package com.ekylibre.android.utils;


import android.content.Context;
import android.content.res.Resources;

import com.ekylibre.android.database.models.Storage;
import com.ekylibre.android.type.EquipmentTypeEnum;
import com.ekylibre.android.type.InterventionOutputTypeEnum;
import com.ekylibre.android.type.SpecieEnum;
import com.ekylibre.android.type.StorageTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Enums {

    public static List<EquipmentTypeEnum> EQUIMPMENT_ENUMS = new ArrayList<>(Arrays.asList(EquipmentTypeEnum.values()));
    public static List<String> EQUIMPMENT_TYPES = new ArrayList<>();
    public static List<String> EQUIMPMENT_NAMES = new ArrayList<>();

    public static List<InterventionOutputTypeEnum> OUTPUT_ENUMS = new ArrayList<>(Arrays.asList(InterventionOutputTypeEnum.values()));
    public static List<String> OUTPUT_TYPES = new ArrayList<>();
    public static List<String> OUTPUT_NAMES = new ArrayList<>();

    public static List<SpecieEnum> SPECIE_ENUMS = new ArrayList<>(Arrays.asList(SpecieEnum.values()));
    public static List<String> SPECIE_TYPES = new ArrayList<>();
    public static List<String> SPECIE_NAMES = new ArrayList<>();

    public static List<StorageTypeEnum> STORAGE_TYPE_ENUMS = new ArrayList<>(Arrays.asList(StorageTypeEnum.values()));
    public static List<String> STORAGE_TYPE_VALUES = new ArrayList<>();
    public static List<String> STORAGE_TYPE_NAMES = new ArrayList<>();

    // Custom lists
    public static List<Storage> STORAGE_LIST = new ArrayList<>();
    public static List<String> STORAGE_LIST_NAMES = new ArrayList<>();


    public static void generateStorages(Context context) {
        STORAGE_LIST_NAMES.clear();
        for (Storage storage : STORAGE_LIST)
            STORAGE_LIST_NAMES.add(String.format("%s (%s)",storage.name, translate(context, storage.type)));
    }

    public static int getIndex(String name) {
        for (Storage storage : STORAGE_LIST)
            if (storage.name.equals(name))
                return STORAGE_LIST.indexOf(storage);
        return 0;
    }

    public static void buildEnumsTranslation(Context context) {

        EQUIMPMENT_ENUMS.remove(EquipmentTypeEnum.$UNKNOWN);
        for (EquipmentTypeEnum item : EQUIMPMENT_ENUMS) {
            EQUIMPMENT_TYPES.add(item.rawValue());
            EQUIMPMENT_NAMES.add(translate(context, item.rawValue()));
        }

        OUTPUT_ENUMS.remove(InterventionOutputTypeEnum.$UNKNOWN);
        for (InterventionOutputTypeEnum item : OUTPUT_ENUMS) {
            OUTPUT_TYPES.add(item.rawValue());
            OUTPUT_NAMES.add(translate(context, item.rawValue()));
        }

        SPECIE_ENUMS.remove(SpecieEnum.$UNKNOWN);
        for (SpecieEnum item : SPECIE_ENUMS) {
            SPECIE_TYPES.add(item.rawValue());
            SPECIE_NAMES.add(translate(context, item.rawValue()));
        }

        STORAGE_TYPE_ENUMS.remove(StorageTypeEnum.$UNKNOWN);
        for (StorageTypeEnum item : STORAGE_TYPE_ENUMS) {
            STORAGE_TYPE_VALUES.add(item.rawValue());
            STORAGE_TYPE_NAMES.add(translate(context, item.rawValue()));
        }
    }

    private static String translate(Context ctx, String rawValue) {
        String translation;
        try {
            translation = ctx.getString(ctx.getResources().getIdentifier(rawValue, "string", ctx.getPackageName()));
        } catch (Resources.NotFoundException e) {
            translation = rawValue;
        }
        return translation;
    }
}
