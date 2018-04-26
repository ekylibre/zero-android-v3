package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Intervention;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionFertilizer.TABLE_NAME, foreignKeys = {

        @ForeignKey(entity = Intervention.class,
            parentColumns = Intervention.COLUMN_ID,
            childColumns = InterventionFertilizer.COLUMN_INTERVENTION_ID, onDelete = CASCADE
        ),

        @ForeignKey(entity = Fertilizer.class,
            parentColumns = Fertilizer.COLUMN_ID,
            childColumns = InterventionFertilizer.COLUMN_FERTI_ID
        )},
        primaryKeys = { InterventionFertilizer.COLUMN_INTERVENTION_ID, InterventionFertilizer.COLUMN_FERTI_ID})
public class InterventionFertilizer {

    public static final String TABLE_NAME = "interventions_ferti";
    public static final String COLUMN_INTERVENTION_ID = "intervention" + BaseColumns._ID;
    public static final String COLUMN_FERTI_ID = "ferti" + BaseColumns._ID;

    public float quantity;

    public String unit;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    @NonNull public Integer intervention_id;

    @ColumnInfo(name = COLUMN_FERTI_ID, index = true)
    @NonNull public Integer ferti_id;

    public InterventionFertilizer(float quantity, String unit, @NonNull Integer intervention_id, @NonNull Integer ferti_id) {
        this.quantity = quantity;
        this.unit = unit;
        this.intervention_id = intervention_id;
        this.ferti_id = ferti_id;
    }

    @Ignore
    public InterventionFertilizer(int fertiId) {
        this.quantity = 0f;
        this.unit = "KILOGRAM_PER_HECTARE";
        this.intervention_id = -1;
        this.ferti_id = fertiId;
    }

}