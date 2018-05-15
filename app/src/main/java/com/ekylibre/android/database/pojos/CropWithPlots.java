package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Plot;

import java.util.List;

public class CropWithPlots {

    @Embedded
    public Crop crop;

    @Relation(parentColumn = Crop.COLUMN_PLOT, entityColumn = Plot.COLUMN_UUID, entity = Plot.class)
    public List<Plot> plots;

//    @Relation(parentColumn = Crop.COLUMN_SUBPLOT, entityColumn = Subplot.COLUMN_UUID, entity = Subplot.class)
//    public List<Subplot> subplots;

    public CropWithPlots() {}

}
