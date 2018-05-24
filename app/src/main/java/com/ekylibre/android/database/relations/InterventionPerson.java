package com.ekylibre.android.database.relations;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Person;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = InterventionPerson.TABLE_NAME, primaryKeys = { InterventionPerson.COLUMN_INTERVENTION_ID, InterventionPerson.COLUMN_PERSON_ID}, foreignKeys = {
@ForeignKey(entity = Intervention.class, parentColumns = Intervention.COLUMN_ID, childColumns = InterventionPerson.COLUMN_INTERVENTION_ID, onDelete = CASCADE),
@ForeignKey(entity = Person.class, parentColumns = Person.COLUMN_ID, childColumns = InterventionPerson.COLUMN_PERSON_ID)})
public class InterventionPerson {

    public static final String TABLE_NAME = "intervention_persons";
    public static final String COLUMN_INTERVENTION_ID = "intervention_id";
    public static final String COLUMN_PERSON_ID = "person_id";

    @NonNull
    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer intervention_id;

    @NonNull
    @ColumnInfo(name = COLUMN_PERSON_ID, index = true)
    public Integer person_id;

    public Boolean is_driver;

    public InterventionPerson(@NonNull Integer intervention_id, @NonNull Integer person_id, Boolean is_driver) {
        this.intervention_id = intervention_id;
        this.person_id = person_id;
        this.is_driver = is_driver;
    }

    @Ignore
    public InterventionPerson(int personId) {
        this.intervention_id = -1;
        this.person_id = personId;
        this.is_driver = false;
    }

}