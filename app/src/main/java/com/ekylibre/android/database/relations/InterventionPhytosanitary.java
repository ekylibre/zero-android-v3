package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Phyto;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionPhytosanitary.TABLE_NAME, foreignKeys = {

        @ForeignKey(entity = Intervention.class,
            parentColumns = Intervention.COLUMN_ID,
            childColumns = InterventionPhytosanitary.COLUMN_INTERVENTION_ID, onDelete = CASCADE
        ),

        @ForeignKey(entity = Phyto.class,
            parentColumns = Phyto.COLUMN_ID,
            childColumns = InterventionPhytosanitary.COLUMN_PHYTO_ID
        )},
        primaryKeys = { InterventionPhytosanitary.COLUMN_INTERVENTION_ID, InterventionPhytosanitary.COLUMN_PHYTO_ID })
public class InterventionPhytosanitary {

    public static final String TABLE_NAME = "intervention_phytosanitary";

    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_PHYTO_ID = "phytosanitary_id";

    @ColumnInfo(name = COLUMN_QUANTITY)
    public float quantity;

    @ColumnInfo(name = COLUMN_UNIT)
    public String unit;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    @NonNull public Integer intervention_id;

    @ColumnInfo(name = COLUMN_PHYTO_ID, index = true)
    @NonNull public Integer phyto_id;

    public InterventionPhytosanitary(float quantity, String unit, @NonNull Integer intervention_id, @NonNull Integer phyto_id) {
        this.quantity = quantity;
        this.unit = unit;
        this.intervention_id = intervention_id;
        this.phyto_id = phyto_id;
    }

    @Ignore
    public InterventionPhytosanitary(int phytoId) {
        this.quantity = 0f;
        this.unit = "LITER_PER_HECTARE";
        this.intervention_id = -1;
        this.phyto_id = phytoId;

    }

}