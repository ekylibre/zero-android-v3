package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;


@Entity(tableName = Farm.TABLE_NAME)
public class Farm {

    public static final String TABLE_NAME = "farms";

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_NAME = "variety";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_TOKEN = "token";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID, index = true)
    public Integer id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_ACTIVE)
    public Integer active;

    @ColumnInfo(name = COLUMN_EMAIL)
    public String email;

    @ColumnInfo(name = COLUMN_TOKEN)
    public String token;
}