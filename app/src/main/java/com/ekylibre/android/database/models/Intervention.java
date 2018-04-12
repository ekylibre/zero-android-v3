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

    public String procedure;

    public String detail;

    public Date date;

    public Integer duration;

    public String harvest_products;

    public Integer farm;

    public Integer weather_temp;

    public Integer weather_wind;

    public Integer weather_cond;

    public String comment;

    public Integer user;

    public Integer status;

    public Date status_time;


    public Intervention(String procedure, String detail, Date date, Integer duration,
                        String harvest_products, Integer farm, Integer weather_temp,
                        Integer weather_wind, Integer weather_cond, String comment,
                        Integer user, Integer status, Date status_time) {

        this.procedure = procedure;
        this.detail = detail;
        this.date = date;
        this.duration = duration;
        this.harvest_products = harvest_products;
        this.farm = farm;
        this.weather_temp = weather_temp;
        this.weather_wind = weather_wind;
        this.weather_cond = weather_cond;
        this.comment = comment;
        this.user = user;
        this.status = status;
        this.status_time = status_time;
    }
}