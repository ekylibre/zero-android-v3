package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

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
