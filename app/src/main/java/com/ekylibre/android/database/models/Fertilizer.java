package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Fertilizer.TABLE_NAME)
public class Fertilizer {

    public static final String TABLE_NAME = "fertilizers";
    public static final String COLUMN_ID = "fertilizer_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    public String name;

    @ColumnInfo(index = true)
    public String label_fra;

    public String variant;

    public String variety;

    public String derivative_of;

    public String nature;

    public String nitrogen_concentration;

    public String phosphorus_concentration;

    public String potassium_concentration;

    public String sulfur_trioxyde_concentration;

    @ColumnInfo(index = true)
    public Boolean registered;

    @ColumnInfo(index = true)
    public Boolean used;

    public String unit;

    public Fertilizer(Integer id, Integer eky_id, String name, String label_fra, String variant, String variety, String derivative_of, String nature, String nitrogen_concentration, String phosphorus_concentration, String potassium_concentration, String sulfur_trioxyde_concentration, Boolean registered, Boolean used, String unit) {
        this.id = id;
        this.eky_id = eky_id;
        this.name = name;
        this.label_fra = label_fra;
        this.variant = variant;
        this.variety = variety;
        this.derivative_of = derivative_of;
        this.nature = nature;
        this.nitrogen_concentration = nitrogen_concentration;
        this.phosphorus_concentration = phosphorus_concentration;
        this.potassium_concentration = potassium_concentration;
        this.sulfur_trioxyde_concentration = sulfur_trioxyde_concentration;
        this.registered = registered;
        this.used = used;
        this.unit = unit;
    }
}