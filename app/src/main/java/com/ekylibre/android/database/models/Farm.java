package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;


@Entity(tableName = Farm.TABLE_NAME)
public class Farm {

    public static final String TABLE_NAME = "farms";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;

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