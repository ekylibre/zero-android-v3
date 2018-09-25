package com.ekylibre.android.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = Person.TABLE_NAME)
public class Person {

    public static final String TABLE_NAME = "persons";
    public static final String COLUMN_ID = "person_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    @ColumnInfo(index = true)
    public String first_name;

    @ColumnInfo(index = true)
    public String last_name;

    public String role;

    public String farm_id;

    public Person(Integer eky_id, String first_name, String last_name, String farm_id) {
        this.eky_id = eky_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.farm_id = farm_id;
    }
}