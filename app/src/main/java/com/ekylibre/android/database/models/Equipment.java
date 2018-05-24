package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Equipment.TABLE_NAME)
public class Equipment {

    public static final String TABLE_NAME = "equipments";
    public static final String COLUMN_ID = "equipment_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    @ColumnInfo(index = true)
    public String name;

    public String number;

    public String type;

    public String farmId;

    public Equipment(Integer eky_id, String name, String type, String number, String farmId) {
        this.eky_id = eky_id;
        this.name = name;
        this.type = type;
        this.number = number;
        this.farmId = farmId;
    }


}