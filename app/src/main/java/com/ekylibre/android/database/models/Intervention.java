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
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID, index = true)
    public Integer id;
    @ColumnInfo(name = COLUMN_ID_EKY, index = true)
    public Integer eky_id;
    public String type;
    public Integer output;
    public Integer weather_temp;
    public Integer weather_wind;
    public Integer weather_desc;
    public Integer water_quantity;
    public String water_unit;
    public String farm;
    public String comment;  // not yet implemented in api
    public String status;

/*    public Intervention(String type, Integer output, Integer weather_temp, Integer weather_wind, Integer weather_desc, Float water_quantity, String water_unit, String farm, String comment) {
        this.type = type;
        this.output = output;
        this.weather_temp = weather_temp;
        this.weather_wind = weather_wind;
        this.weather_desc = weather_desc;
        this.water_quantity = water_quantity;
        this.water_unit = water_unit;
        this.farm = farm;
        this.comment = comment;
    }*/

    public Integer getEky_id() {
        return eky_id;
    }

    public void setEky_id(Integer eky_id) {
        this.eky_id = eky_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOutput() {
        return output;
    }

    public void setOutput(Integer output) {
        this.output = output;
    }

    public Integer getWeather_temp() {
        return weather_temp;
    }

    public void setWeather_temp(Integer weather_temp) {
        this.weather_temp = weather_temp;
    }

    public Integer getWeather_wind() {
        return weather_wind;
    }

    public void setWeather_wind(Integer weather_wind) {
        this.weather_wind = weather_wind;
    }

    public Integer getWeather_desc() {
        return weather_desc;
    }

    public void setWeather_desc(Integer weather_desc) {
        this.weather_desc = weather_desc;
    }

    public Integer getWater_quantity() {
        return water_quantity;
    }

    public void setWater_quantity(Integer water_quantity) {
        this.water_quantity = water_quantity;
    }

    public String getWater_unit() {
        return water_unit;
    }

    public void setWater_unit(String water_unit) {
        this.water_unit = water_unit;
    }

    public String getFarm() {
        return farm;
    }

    public void setFarm(String farm) {
        this.farm = farm;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}