package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Intervention.TABLE_NAME)
public class Intervention {

    public static final String TABLE_NAME = "interventions";
    public static final String COLUMN_ID = "intervention_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID, index = true)
    public Integer id;
    @ColumnInfo(name = COLUMN_ID_EKY, index = true)
    public Integer eky_id;
    public String type;
    public Integer output;

    public Integer water_quantity;
    public String water_unit;
    public String farm;
    public String comment;  // not yet implemented in api
    public String status;

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