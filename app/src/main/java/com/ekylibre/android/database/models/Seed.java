package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;


//, foreignKeys =
//@ForeignKey(entity = Specie.class,
//        parentColumns = Specie.COLUMN_NAME,
//        childColumns = Seed.COLUMN_SPECIE)

@Entity(tableName = Seed.TABLE_NAME)
public class Seed {

    public static final String TABLE_NAME = "seeds";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;
    public static final String COLUMN_SPECIE = "specie";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_SPECIE, index = true)
    public String specie;

    @ColumnInfo(index = true)
    public String variety;

    @ColumnInfo(index = true)
    public Boolean registered;

    @ColumnInfo(index = true)
    public Boolean used;

    @Ignore
    public String fra;

    public Seed(Integer id, String specie, String variety, Boolean registered, Boolean used) {
        this.id = id;
        this.specie = specie;
        this.variety = variety;
        this.registered = registered;
        this.used = used;
    }

}