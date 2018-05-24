package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Seed;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionSeed.TABLE_NAME, foreignKeys = {

        @ForeignKey(entity = Intervention.class,
            parentColumns = Intervention.COLUMN_ID,
            childColumns = InterventionSeed.COLUMN_INTERVENTION_ID, onDelete = CASCADE
        ),

        @ForeignKey(entity = Seed.class,
            parentColumns = Seed.COLUMN_ID,
            childColumns = InterventionSeed.COLUMN_SEED_ID
        )},
        primaryKeys = { InterventionSeed.COLUMN_INTERVENTION_ID, InterventionSeed.COLUMN_SEED_ID})
public class InterventionSeed {

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

    public InterventionSeed(float quantity, String unit, @NonNull Integer intervention_id, @NonNull Integer seed_id) {
        this.quantity = quantity;
        this.unit = unit;
        this.intervention_id = intervention_id;
        this.seed_id = seed_id;
    }

    @Ignore
    public InterventionSeed(int seedId) {
        this.quantity = 0f;
        this.unit = "KILOGRAM_PER_HECTARE";
        this.intervention_id = -1;
        this.seed_id = seedId;
    }

}