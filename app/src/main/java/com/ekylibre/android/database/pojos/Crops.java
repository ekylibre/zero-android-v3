package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.relations.InterventionCrop;

import java.util.List;

public class Crops {

    @Embedded
    public InterventionCrop inter;

    @Relation(parentColumn = InterventionCrop.COLUMN_CROP_ID, entityColumn = Crop.COLUMN_UUID, entity = Crop.class)
    public List<CropWithPlots> cropWithPlots;

    public Crops() {}

}
