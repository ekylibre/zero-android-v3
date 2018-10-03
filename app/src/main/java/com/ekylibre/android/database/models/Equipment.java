package com.ekylibre.android.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


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

    public String field1Value;

    public String field2Value;

    public Equipment(Integer eky_id, String name, String type, String number, String farmId, String field1Value, String field2Value) {
        this.eky_id = eky_id;
        this.name = name;
        this.type = type;
        this.number = number;
        this.farmId = farmId;
        this.field1Value = field1Value;
        this.field2Value = field2Value;

    }


}