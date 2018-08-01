package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Material.TABLE_NAME)
public class Material {

    public static final String TABLE_NAME = "materials";
    public static final String COLUMN_ID = "material_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    @ColumnInfo(index = true)
    public String name;

    public String description;

    public String unit;

    public Material(Integer eky_id, String name, String description, String unit) {
        this.eky_id = eky_id;
        this.name = name;
        this.description = description;
        this.unit = unit;
    }


}