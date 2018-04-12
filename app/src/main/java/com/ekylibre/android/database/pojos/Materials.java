package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.relations.InterventionMaterial;

import java.util.List;

public class Materials {

    @Embedded
    public InterventionMaterial inter;

    @Relation(parentColumn = InterventionMaterial.COLUMN_MATERIAL_ID, entityColumn = Material.COLUMN_ID)
    public List<Material> material;

    public Materials() {}

}
