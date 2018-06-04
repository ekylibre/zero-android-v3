package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Storage.TABLE_NAME)
public class Storage {

    public static final String TABLE_NAME = "storages";
    public static final String COLUMN_ID = "storage_id";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;
    public String name;
    public String type;

    public Storage(Integer id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}