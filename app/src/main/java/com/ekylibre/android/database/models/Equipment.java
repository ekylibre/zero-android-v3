package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;


@Entity(tableName = Equipment.TABLE_NAME)
public class Equipment {

    public static final String TABLE_NAME = "equipments";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(index = true)
    public String name;

    public String type;

    public String farmId;

    public Equipment(String name, String type) {
        this.name = name;
        this.type = type;
    }


}