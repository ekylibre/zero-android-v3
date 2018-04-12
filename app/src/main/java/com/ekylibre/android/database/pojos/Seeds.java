package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.relations.InterventionSeed;

import java.util.List;

public class Seeds {

    @Embedded
    public InterventionSeed inter;

    @Relation(parentColumn = InterventionSeed.COLUMN_SEED_ID, entityColumn = Seed.COLUMN_ID)
    public List<Seed> seed;

    public Seeds() {}

}
