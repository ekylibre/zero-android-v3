package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;


@Entity(tableName = Material.TABLE_NAME)
public class Material {

    public static final String TABLE_NAME = "materials";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(index = true)
    public String name;

    public String description;

    public Material(String name, String description) {
        this.name = name;
        this.description = description;
    }


}