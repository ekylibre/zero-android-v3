package com.ekylibre.android.adapters.CropInfo;

/**
 * Created by Rémi de Chazelles on 23/07/18.
 */
public class ProductionItem extends ListItem {

    private String name;
    private float surface;

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

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
