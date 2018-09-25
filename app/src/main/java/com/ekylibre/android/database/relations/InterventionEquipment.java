package com.ekylibre.android.database.relations;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;

import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Intervention;

import static androidx.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionEquipment.TABLE_NAME, primaryKeys = { InterventionEquipment.COLUMN_INTERVENTION_ID, InterventionEquipment.COLUMN_EQUIPMENT_ID}, foreignKeys = {
@ForeignKey(entity = Intervention.class, parentColumns = Intervention.COLUMN_ID, childColumns = InterventionEquipment.COLUMN_INTERVENTION_ID, onDelete = CASCADE),
@ForeignKey(entity = Equipment.class, parentColumns = Equipment.COLUMN_ID, childColumns = InterventionEquipment.COLUMN_EQUIPMENT_ID)})
public class InterventionEquipment {

    public static final String TABLE_NAME = "intervention_equipments";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_EQUIPMENT_ID = "equipment_id";

    @NonNull
    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer intervention_id;

    @NonNull
    @ColumnInfo(name = COLUMN_EQUIPMENT_ID, index = true)
    public Integer equipment_id;

    public InterventionEquipment(@NonNull Integer intervention_id, @NonNull Integer equipment_id) {
        this.intervention_id = intervention_id;
        this.equipment_id = equipment_id;
    }

    @Ignore
    public InterventionEquipment(int equipmentId) {
        this.intervention_id = -1;
        this.equipment_id = equipmentId;
    }

}