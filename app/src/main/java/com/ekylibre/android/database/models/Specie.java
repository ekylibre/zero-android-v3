package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;


@Entity(tableName = Specie.TABLE_NAME)
public class Specie {

    public static final String TABLE_NAME = "species";
    public static final String COLUMN_NAME = "name";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_NAME, index = true)
    @NonNull public String name;

    @ColumnInfo(index = true)
    public String fra;

    @ColumnInfo(index = true)
    public String eng;

    public Specie(@NonNull String name, String fra, String eng) {
        this.name = name;
        this.fra = fra;
        this.fra = eng;
    }

}