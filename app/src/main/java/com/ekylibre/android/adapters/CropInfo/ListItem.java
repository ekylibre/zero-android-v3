package com.ekylibre.android.adapters.CropInfo;

/**
 * Created by Rémi de Chazelles on 23/07/18.
 */
public abstract class ListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CROP = 1;

    abstract public int getType();
}
