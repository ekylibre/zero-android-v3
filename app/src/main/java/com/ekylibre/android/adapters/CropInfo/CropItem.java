package com.ekylibre.android.adapters.CropInfo;

import com.ekylibre.android.database.models.Intervention;
import com.mapbox.geojson.Point;

import java.util.Date;
import java.util.List;

/**
 * Created by Rémi de Chazelles on 23/07/18.
 */
public class CropItem extends ListItem {

    private String name;
    private String UUID;
    private String production;
    private float surface;
    private Date startDate;
    private Date stopDate;
    private String yield;
    private Double distance;
    private Point centroid;
    private List<Intervention> interventions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public float getSurface() {
        return surface;
    }

    public void setSurface(float surface) {
        this.surface = surface;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public String getYield() {
        return yield;
    }

    public void setYield(String yield) {
        this.yield = yield;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Point getCentroid() {
        return centroid;
    }

    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }

    public List<Intervention> getInterventions() {
        return interventions;
    }

    public void setInterventions(List<Intervention> interventions) {
        this.interventions = interventions;
    }

    @Override
    public int getType() {
        return TYPE_CROP;
    }

    public CropItem(String name, String UUID, String production, float surface, Date startDate, Date stopDate, String yield, Point centroid, List<Intervention> interventions) {
        this.name = name;
        this.UUID = UUID;
        this.production = production;
        this.surface = surface;
        this.startDate = startDate;
        this.stopDate = stopDate;
        this.yield = yield;
        this.centroid = centroid;
        this.interventions = interventions;
    }
}
