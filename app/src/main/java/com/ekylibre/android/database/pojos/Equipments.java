package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.relations.InterventionEquipment;

import java.util.List;

public class Equipments {

    @Embedded
    public InterventionEquipment inter;

    @Relation(parentColumn = InterventionEquipment.COLUMN_EQUIPMENT_ID, entityColumn = Equipment.COLUMN_ID)
    public List<Equipment> equipment;

    public Equipments() {}

}
