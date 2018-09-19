package com.ekylibre.android.database.pojos;

import com.ekylibre.android.database.models.Crop;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class CropsByPlot {

    public String name;
    public boolean is_checked;
    public List<Crop> crops;

    public CropsByPlot(String name) {
        this.name = name;
        this.is_checked = false;
        this.crops = new ArrayList<>();
    }

    public Float getSurface(boolean onlySelected) {
        Timber.i("getSurface");
        if (!crops.isEmpty()) {
            float total = 0;
            for (Crop crop : crops) {
                if (onlySelected) {
                    if (crop.is_checked)
                        total += crop.surface_area;
                }
                else
                    total += crop.surface_area;
            }
            return total;
        }

        return null;
    }

}
