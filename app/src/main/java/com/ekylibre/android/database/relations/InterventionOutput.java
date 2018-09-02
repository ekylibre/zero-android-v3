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
            childColumns = InterventionOutput.COLUMN_OUTPUT_ID
        )},
        primaryKeys = { InterventionOutput.COLUMN_INTERVENTION_ID, InterventionOutput.COLUMN_OUTPUT_ID})
public class InterventionOutput {

    public static final String TABLE_NAME = "intervention_outputs";
    public static final String COLUMN_INTERVENTION_ID = "id_intervention";
    public static final String COLUMN_OUTPUT_ID = "id_output";

    public float quantity;

    public String unit;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer id_intervention;

    @ColumnInfo(name = COLUMN_OUTPUT_ID, index = true)
    public Integer id_output;

    @Ignore
    public Seed seed;

    public InterventionOutput(float quantity, String unit, Integer id_intervention, Integer id_output) {
        this.quantity = quantity;
        this.unit = unit;
        this.id_intervention = id_intervention;
        this.id_output = id_output;
    }

    @Ignore
    public InterventionOutput() {}

    public void setInter(float quantity, String unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

}