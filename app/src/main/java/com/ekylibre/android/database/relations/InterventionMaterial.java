package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Material;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionMaterial.TABLE_NAME, primaryKeys = { InterventionMaterial.COLUMN_INTERVENTION_ID, InterventionMaterial.COLUMN_MATERIAL_ID}, foreignKeys = {
@ForeignKey(entity = Intervention.class, parentColumns = Intervention.COLUMN_ID, childColumns = InterventionMaterial.COLUMN_INTERVENTION_ID, onDelete = CASCADE),
@ForeignKey(entity = Material.class, parentColumns = Material.COLUMN_ID, childColumns = InterventionMaterial.COLUMN_MATERIAL_ID)})
public class InterventionMaterial {

    public static final String TABLE_NAME = "intervention_materials";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_MATERIAL_ID = "material_id";

    public Integer quantity;

    public String unit;

    public Boolean approximative_value;

    @NonNull
    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer intervention_id;

    @NonNull
    @ColumnInfo(name = COLUMN_MATERIAL_ID, index = true)
    public Integer material_id;

    public InterventionMaterial(Integer quantity, String unit, @NonNull Integer intervention_id, @NonNull Integer material_id, Boolean approximative_value) {
        this.quantity = quantity;
        this.unit = unit;
        this.intervention_id = intervention_id;
        this.material_id = material_id;
        this.approximative_value = approximative_value;
    }

    @Ignore
    public InterventionMaterial(int materialId) {
        this.quantity = 0;
        this.unit = "UNITY";
        this.intervention_id = -1;
        this.material_id = materialId;
        this.approximative_value = false;
    }

}