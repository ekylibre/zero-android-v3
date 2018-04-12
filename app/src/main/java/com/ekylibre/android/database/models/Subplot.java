package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.Date;


@Entity(tableName = Subplot.TABLE_NAME)
public class Subplot {

    public static final String TABLE_NAME = "subplots";
    public static final String COLUMN_UUID = "uuid";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = COLUMN_UUID)
    public String uuid;

    public String shape;

    public float surface_area;

    public String centroid;

    public Date startDate;

    public Date stopDate;

    public String plot;

    public Integer farm;

    public Subplot(@NonNull String uuid, String shape, float surface_area,
                   String centroid, Date startDate, Date stopDate, String plot, Integer farm) {
        this.uuid = uuid;
        this.shape = shape;
        this.surface_area = surface_area;
        this.centroid = centroid;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.plot = plot;
        this.farm = farm;
    }
}
