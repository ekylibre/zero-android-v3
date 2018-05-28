package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.utils.Unit;
import com.ekylibre.android.utils.Units;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionOutput.TABLE_NAME, foreignKeys = {

        @ForeignKey(entity = Intervention.class,
            parentColumns = Intervention.COLUMN_ID,
            childColumns = InterventionOutput.COLUMN_INTERVENTION_ID, onDelete = CASCADE
        ),

        @ForeignKey(entity = Harvest.class,
            parentColumns = Harvest.COLUMN_ID,
            childColumns = InterventionOutput.COLUMN_SEED_ID
        )},
        primaryKeys = { InterventionOutput.COLUMN_INTERVENTION_ID, InterventionOutput.COLUMN_SEED_ID})
public class InterventionOutput {

    public static final String TABLE_NAME = "intervention_seeds";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_SEED_ID = "seed_id";

    public float quantity;

    public String unit;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    @NonNull public Integer intervention_id;

    @ColumnInfo(name = COLUMN_SEED_ID, index = true)
    @NonNull public Integer seed_id;

    @Ignore
    public Seed seed;

    public InterventionOutput(float quantity, String unit, @NonNull Integer intervention_id, @NonNull Integer seed_id) {
        this.quantity = quantity;
        this.unit = unit;
        this.intervention_id = intervention_id;
        this.seed_id = seed_id;
    }

    @Ignore
    public InterventionOutput(int seedId) {
        this.quantity = 0f;
        this.unit = "KILOGRAM_PER_HECTARE";
        this.intervention_id = -1;
        this.seed_id = seedId;
    }

    public Unit getUnit() {
        return Units.getUnit(unit);
    }

    public void setInter(float quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

}