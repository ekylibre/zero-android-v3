package com.ekylibre.android.adapters.CropInfo;

import com.ekylibre.android.database.models.Intervention;

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
}
