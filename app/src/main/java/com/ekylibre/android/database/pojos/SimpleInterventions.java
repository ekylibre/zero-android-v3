package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionWorkingDay;

import java.util.List;

public class SimpleInterventions {

    @Embedded
    public Intervention intervention;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionWorkingDay.COLUMN_INTERVENTION_ID, entity = InterventionWorkingDay.class)
    public List<InterventionWorkingDay> workingDays;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionCrop.COLUMN_INTERVENTION_ID, entity = InterventionCrop.class)
    public List<Crops> crops;

}