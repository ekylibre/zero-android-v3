package com.ekylibre.android.database.relations;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Crop;

import static androidx.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionCrop.TABLE_NAME, primaryKeys = { InterventionCrop.COLUMN_INTERVENTION_ID, InterventionCrop.COLUMN_CROP_ID}, foreignKeys = {
@ForeignKey(entity = Intervention.class, parentColumns = Intervention.COLUMN_ID, childColumns = InterventionCrop.COLUMN_INTERVENTION_ID, onDelete = CASCADE),
@ForeignKey(entity = Crop.class, parentColumns = Crop.COLUMN_UUID, childColumns = InterventionCrop.COLUMN_CROP_ID)})
public class InterventionCrop {

    public static final String TABLE_NAME = "intervention_crops";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_CROP_ID = "crop_id" + BaseColumns._ID;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    @NonNull public Integer intervention_id;

    @ColumnInfo(name = COLUMN_CROP_ID, index = true)
    @NonNull public String crop_id;

    public Integer work_area_percentage;

    public InterventionCrop(@NonNull Integer intervention_id, @NonNull String crop_id, Integer work_area_percentage) {
        this.intervention_id = intervention_id;
        this.crop_id = crop_id;
        this.work_area_percentage = work_area_percentage;
    }

    @Ignore
    public InterventionCrop(@NonNull String cropId) {
        this.intervention_id = -1;
        this.crop_id = cropId;
        this.work_area_percentage = 100;
    }

}