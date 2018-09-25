package com.ekylibre.android.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

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
