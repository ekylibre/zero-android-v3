package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Plot;

import java.util.List;


public class Plots {

    @Embedded
    public Plot plot;

    @Relation(parentColumn = Plot.COLUMN_UUID, entityColumn = Crop.COLUMN_PLOT)
    public List<Crop> crops;

    public Plots(Plot plot) {
        this.plot = plot;
    }

}
