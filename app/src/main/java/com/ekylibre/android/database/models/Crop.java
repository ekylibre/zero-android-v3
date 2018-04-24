package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;


@Entity(tableName = Crop.TABLE_NAME, foreignKeys = {

//    @ForeignKey(entity = Plot.class,
//            parentColumns = Plot.COLUMN_UUID,
//            childColumns = Crop.COLUMN_PLOT),
    @ForeignKey(entity = Farm.class,
            parentColumns = Farm.COLUMN_ID,
            childColumns = Crop.COLUMN_FARM)
})
public class Crop {

    public static final String TABLE_NAME = "crops";
    public static final String COLUMN_UUID = TABLE_NAME + "_uuid";
    public static final String COLUMN_FARM = "farm";
    public static final String COLUMN_PLOT = "plot";
    public static final String COLUMN_SUBPLOT = "subplot";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = COLUMN_UUID)
    public String uuid;

    @ColumnInfo(index = true)
    public String name;

    public String specie;

    public String production_nature;

    public String production_mode;

    public String production_output;

    public String provisional_yield;  // Measure{unit: ton_per_hectare}

    public String shape;

    public float surface_area;

    public String centroid;

    public Date start_date;

    public Date stop_date;

    @ColumnInfo(name = COLUMN_PLOT, index = true)
    public String plot;

    @ColumnInfo(name = COLUMN_SUBPLOT, index = true)
    public String subplot;

    @ColumnInfo(name = COLUMN_FARM, index = true)
    public String farm;

    @Ignore
    public Boolean is_checked;

    @Ignore
    public int work_area_percentage;

    public Crop(@NonNull String uuid, String name, String specie, String production_nature,
                String production_mode, String production_output, String provisional_yield,
                String shape, float surface_area, String centroid, Date start_date, Date stop_date,
                String plot, String subplot,
                String farm) {

        this.uuid = uuid;
        this.name = name;
        this.specie = specie;
        this.production_nature = production_nature;
        this.production_mode = production_mode;
        this.production_output = production_output;
        this.provisional_yield = provisional_yield;
        this.shape = shape;
        this.surface_area = surface_area;
        this.centroid = centroid;
        this.start_date = start_date;
        this.stop_date = stop_date;
        this.plot = plot;
        this.subplot = subplot;
        this.farm = farm;
        this.is_checked = false;
        this.work_area_percentage = 100;
    }
}
