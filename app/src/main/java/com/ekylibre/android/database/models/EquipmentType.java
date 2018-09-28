package com.ekylibre.android.database.models;

import com.squareup.moshi.Json;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = EquipmentType.TABLE_NAME)
public class EquipmentType {

    public static final String TABLE_NAME = "equipment_types";
    public static final String COLUMN_ID = "id";

    @PrimaryKey()
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @Json(name="fra")
    public String name;

    @Json(name="main_frozen_indicator_name")
    public String field_1_name;

    @Json(name="main_frozen_indicator_unit")
    public String field_1_unit;

    @Json(name="other_frozen_indicator_name")
    public String field_2_name;

    @Json(name="other_frozen_indicator_unit")
    public String field_2_unit;

    @Json(name="nature")
    public String type;

    public EquipmentType(Integer id, String name, String field_1_name, String field_1_unit, String field_2_name, String field_2_unit, String type) {
        this.id = id;
        this.name = name;
        this.field_1_name = field_1_name;
        this.field_1_unit = field_1_unit;
        this.field_2_name = field_2_name;
        this.field_2_unit = field_2_unit;
        this.type = type;
    }
}