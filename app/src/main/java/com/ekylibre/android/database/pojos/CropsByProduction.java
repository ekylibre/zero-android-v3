package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.relations.InterventionCrop;

import java.util.List;

public class CropsByProduction {

    @Embedded
    public Crop crop;

    @Relation(parentColumn = Crop.COLUMN_UUID, entityColumn = InterventionCrop.COLUMN_CROP_ID, entity = InterventionCrop.class)
    public List<InterventionCrop> interventionCrop;

}
