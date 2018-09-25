package com.ekylibre.android.database.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "points")
public class Point {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public long time;
    public double lat;
    public double lon;
    public float speed;
    public int accuracy;
    public String type;
    public int intervention_id;

    public Point(long time, double lat, double lon, float speed, int accuracy,
                 String type, int intervention_id) {
        this.time = time;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.accuracy = accuracy;
        this.type = type;
        this.intervention_id = intervention_id;
    }
}