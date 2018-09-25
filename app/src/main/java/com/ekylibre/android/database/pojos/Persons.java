package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.relations.InterventionPerson;

import java.util.List;

public class Persons {

    @Embedded
    public InterventionPerson inter;

    @Relation(parentColumn = InterventionPerson.COLUMN_PERSON_ID, entityColumn = Person.COLUMN_ID)
    public List<Person> person;

    public Persons() {}

}
