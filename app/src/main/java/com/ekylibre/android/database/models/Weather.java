package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = Weather.TABLE_NAME,
        foreignKeys = @ForeignKey(
            entity = Intervention.class,
            parentColumns = Intervention.COLUMN_ID,
            childColumns = Weather.COLUMN_INTERVENTION_ID,
            onDelete = CASCADE)
)
public class Weather {

    public static final String TABLE_NAME = "weather";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";

    @PrimaryKey
    @ColumnInfo(index = true, name = COLUMN_INTERVENTION_ID)
    public int intervention_id;

    public String temperature;
    public String wind_speed;
    public String description;

    public Weather(int intervention_id, String temperature, String wind_speed, String description) {
        this.intervention_id = intervention_id;
        this.temperature = temperature;
        this.wind_speed = wind_speed;
        this.description = description;
    }
}