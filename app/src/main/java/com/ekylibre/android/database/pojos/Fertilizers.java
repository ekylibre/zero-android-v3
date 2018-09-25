package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.relations.InterventionFertilizer;

import java.util.List;

public class Fertilizers {

    @Embedded
    public InterventionFertilizer inter;

    @Relation(parentColumn = InterventionFertilizer.COLUMN_FERTI_ID, entityColumn = Fertilizer.COLUMN_ID)
    public List<Fertilizer> fertilizer;

    public Fertilizers() {}

}
