package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;


@Entity(tableName = Person.TABLE_NAME)
public class Person {

    public static final String TABLE_NAME = "persons";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(index = true)
    public String first_name;

    @ColumnInfo(index = true)
    public String last_name;

    public String description;

    public Person(String first_name, String last_name, String description) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.description = description;
    }
}