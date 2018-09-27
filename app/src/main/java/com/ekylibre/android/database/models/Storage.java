package com.ekylibre.android.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;


@Entity(tableName = Storage.TABLE_NAME)
public class Storage {

    public static final String TABLE_NAME = "storages";
    public static final String COLUMN_ID = "storage_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    public String name;
    public String type;
    public String farm;

    public Storage(Integer eky_id, String name, String type, String farm) {
        this.eky_id = eky_id;
        this.name = name;
        this.type = type;
        this.farm = farm;
    }
}