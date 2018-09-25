package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Weather;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionMaterial;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;

import java.util.List;

public class Interventions {

    @Embedded
    public Intervention intervention;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionWorkingDay.COLUMN_INTERVENTION_ID, entity = InterventionWorkingDay.class)
    public List<InterventionWorkingDay> workingDays;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionPhytosanitary.COLUMN_INTERVENTION_ID, entity = InterventionPhytosanitary.class)
    public List<Phytos> phytos;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionSeed.COLUMN_INTERVENTION_ID, entity = InterventionSeed.class)
    public List<Seeds> seeds;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionFertilizer.COLUMN_INTERVENTION_ID, entity = InterventionFertilizer.class)
    public List<Fertilizers> fertilizers;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionMaterial.COLUMN_INTERVENTION_ID, entity = InterventionMaterial.class)
    public List<Materials> materials;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionEquipment.COLUMN_INTERVENTION_ID, entity = InterventionEquipment.class)
    public List<Equipments> equipments;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionPerson.COLUMN_INTERVENTION_ID, entity = InterventionPerson.class)
    public List<Persons> persons;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = Weather.COLUMN_INTERVENTION_ID, entity = Weather.class)
    public List<Weather> weather;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = InterventionCrop.COLUMN_INTERVENTION_ID, entity = InterventionCrop.class)
    public List<Crops> crops;

    @Relation(parentColumn = Intervention.COLUMN_ID, entityColumn = Harvest.COLUMN_INTERVENTION_ID)
    public List<Harvest> harvests;

    public Interventions() {}

}