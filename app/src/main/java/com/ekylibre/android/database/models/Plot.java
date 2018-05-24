package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.Date;


@Entity(tableName = Plot.TABLE_NAME)
public class Plot {

    public static final String TABLE_NAME = "plots";
    public static final String COLUMN_UUID = "plot_uuid";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = COLUMN_UUID)
    public String uuid;

    @ColumnInfo(index = true)
    public String name;

    public String shape;

    public float surface_area;

    public String centroid;

    public Date startDate;

    public Date stopDate;

    public String farm;

    @Ignore
    public Boolean is_checked;

    public Plot(@NonNull String uuid, String name, String shape, float surface_area,
                String centroid, Date startDate, Date stopDate, String farm) {
        this.uuid = uuid;
        this.name = name;
        this.shape = shape;
        this.surface_area = surface_area;
        this.centroid = centroid;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.farm = farm;
        this.is_checked = false;
    }
}
