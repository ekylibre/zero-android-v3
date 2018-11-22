package com.ekylibre.android;

import android.content.Context;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.relations.InterventionWorkingDay;
import com.ekylibre.android.type.EquipmentTypeEnum;
import com.ekylibre.android.type.InterventionTypeEnum;
import com.ekylibre.android.type.InterventionWaterVolumeUnitEnum;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.*;


public class DatabaseWriteReadTest {

    private static AppDatabase db;

    private static final String FARM_ID = "0f739219-fb03-46dc-9a02-baa0753364f0";
    private static final String FARM_LABEL = "GAEC du bois joli";

    @BeforeClass
    public static void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = AppDatabase.getInMemoryInstance(context);
        db.populateInitialData(context);
    }

    @AfterClass
    public static void closeDb() {
        db.close();
    }

    @Test
    public void writeEquipmentAndReadInList() {

        db.dao().insert(new Farm(FARM_ID, FARM_LABEL));

        // Insert and test equipment
        Equipment equipment = new Equipment(null, "Tracteur de test",
                EquipmentTypeEnum.TRACTOR.rawValue(), null, FARM_ID, null, null);
        long id = db.dao().insert(equipment);
        assertNotEquals("Problème lors de l'insertion", -1, id);

        List<Equipment> withoutEkyId = db.dao().getEquipmentWithoutEkyId();
        assertEquals(withoutEkyId.get(0).name, "Tracteur de test");
    }

    @Test
    public void writeIntervention() {

        Intervention inter = new Intervention();
        inter.setFarm(FARM_ID);
        inter.setStatus("created");
        inter.setType(InterventionTypeEnum.GROUND_WORK.rawValue());
        inter.setComment("Ceci est un commentaire de test");
        inter.setWater_quantity(5);
        inter.setWater_unit(InterventionWaterVolumeUnitEnum.CUBIC_METER.rawValue());
        int id = (int) (long) db.dao().insert(inter);

        assertNotEquals("Problème lors de l'insertion", -1, id);

        db.dao().insert(new InterventionWorkingDay(id, new Date(), 3.5f));
        db.dao().setInterventionEkyId(id, 123);

        Interventions intervention = db.dao().getIntervention(123);

        assertNotNull("Intervention est null", intervention);
        assertNotNull("Intervention.intervention est null", intervention.intervention);
        assertEquals(3.5f, intervention.workingDays.get(0).hour_duration, 0);
    }
}
