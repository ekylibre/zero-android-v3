package com.ekylibre.android.adapters.CropInfo;

import com.ekylibre.android.database.models.Intervention;

import java.util.List;

/**
 * Created by Rémi de Chazelles on 23/07/18.
 */
public class CropItem extends ListItem {

    private String name;
    private float surface;
    private List<Intervention> interventions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSurface() {
        return surface;
    }

    public void setSurface(float surface) {
        this.surface = surface;
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
