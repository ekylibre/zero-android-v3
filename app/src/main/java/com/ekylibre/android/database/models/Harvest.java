package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = Harvest.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Storage.class,
                parentColumns = Storage.COLUMN_ID,
                childColumns = Harvest.COLUMN_STORAGE_ID,
                onDelete = CASCADE)
)
public class Harvest {

    public static final String TABLE_NAME = "harvests";
    public static final String COLUMN_ID = "harvest_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";
    public static final String COLUMN_STORAGE_ID = "id_storage";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    public float net_quantity;
    public float quantity;
    public String unit;

    @ColumnInfo(name = COLUMN_STORAGE_ID, index = true)
    public Integer storage;

    public Harvest(Integer id, Integer eky_id, float net_quantity, float quantity, String unit, Integer storage) {
        this.id = id;
        this.eky_id = eky_id;
        this.net_quantity = net_quantity;
        this.quantity = quantity;
        this.unit = unit;
        this.storage = storage;
    }
}