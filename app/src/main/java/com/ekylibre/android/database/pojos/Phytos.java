package com.ekylibre.android.database.pojos;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;

import java.util.List;

public class Phytos {

    @Embedded
    public InterventionPhytosanitary inter;

    @Relation(parentColumn = InterventionPhytosanitary.COLUMN_PHYTO_ID, entityColumn = Phyto.COLUMN_ID)
    public List<Phyto> phyto;

    public Phytos() {}

}
