package com.ekylibre.android.database.relations;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionWorkingDay.TABLE_NAME,
        foreignKeys =
                @ForeignKey(
                        entity = Intervention.class,
                        parentColumns = Intervention.COLUMN_ID,
                        childColumns = InterventionWorkingDay.COLUMN_INTERVENTION_ID,
                        onDelete = CASCADE)
)
public class InterventionWorkingDay {

    public static final String TABLE_NAME = "interventions_working_day";
    public static final String COLUMN_INTERVENTION_ID = "intervention" + BaseColumns._ID;
    public static final String COLUMN_ID = "working_day" + BaseColumns._ID;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID, index = true)
    public Integer id;

    @NonNull
    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer intervention_id;

    @NonNull
    public Integer hour_duration;

    @NonNull
    public Date execution_date;

    public InterventionWorkingDay(@NonNull Integer intervention_id, @NonNull Date execution_date, @NonNull Integer hour_duration) {
        this.intervention_id = intervention_id;
        this.execution_date = execution_date;
        this.hour_duration = hour_duration;
    }

}