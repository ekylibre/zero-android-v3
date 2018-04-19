package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import java.util.Date;


@Entity(tableName = Intervention.TABLE_NAME)
public class Intervention {

    public static final String TABLE_NAME = "interventions";
    public static final String COLUMN_ID = TABLE_NAME + BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID, index = true)
    public Integer id;
    public Integer eky_id;
    public String type;
    public String detail;  // not yet implemented in api
    public Date date;
    public Integer duration;
    public Integer output;
    public Integer weather_temp;
    public Integer weather_wind;
    public Integer weather_desc;
    public Integer water_quantity;
    public String water_unit;
    public Integer farm;
    public String comment;  // not yet implemented in api
    public String status;
    public Date status_time;

    public Intervention(String type, Date date, Integer duration, Integer output, Integer weather_temp, Integer weather_wind, Integer weather_desc, Integer water_quantity, String water_unit, Integer farm, String comment) {
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.output = output;
        this.weather_temp = weather_temp;
        this.weather_wind = weather_wind;
        this.weather_desc = weather_desc;
        this.water_quantity = water_quantity;
        this.water_unit = water_unit;
        this.farm = farm;
        this.comment = comment;
    }
}