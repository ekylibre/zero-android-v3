package com.ekylibre.android.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;


@Entity(tableName = Farm.TABLE_NAME)
public class Farm {

    public static final String TABLE_NAME = "farms";
    public static final String COLUMN_ID = "farm_id";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID, index = true)
    public String id;
    public String name;

    public Farm(@NonNull String id, String name) {
        this.id = id;
        this.name = name;
    }
}