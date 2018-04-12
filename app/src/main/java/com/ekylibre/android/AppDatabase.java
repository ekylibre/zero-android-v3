package com.ekylibre.android;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.DAO;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Specie;
import com.ekylibre.android.database.models.Subplot;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionMaterial;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.utils.Converters;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;


@Database(entities = {
        Intervention.class,
        Phyto.class, InterventionPhytosanitary.class,
        Seed.class, InterventionSeed.class, Specie.class,
        Fertilizer.class, InterventionFertilizer.class,
        Material.class, InterventionMaterial.class,
        Equipment.class, InterventionEquipment.class,
        Person.class, InterventionPerson.class,
        Crop.class, InterventionCrop.class,
        Plot.class, Subplot.class
}, exportSchema = false, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    // Log TAG
    private static final String TAG = "AppDatabase";

    // Entities tables
    public abstract DAO dao();

    // Database instance
    private static AppDatabase database;

    public static synchronized AppDatabase getInstance(Context context) {
        if (database == null) database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,"db").build();
        return database;
    }

    /**
     * Populate database with initial Lexicon data
     */
    public void populateInitialData(Context context) {

        try {

            String json;
            database = AppDatabase.getInstance(context);
            Moshi moshi = new Moshi.Builder().build();

            // Load phytos from Lexicon
            json = readJsonFromAssets(context, "lexicon/phytosanitary_products.json");
            Type type = Types.newParameterizedType(List.class, Phyto.class);
            JsonAdapter<List<Phyto>> adapter1 = moshi.adapter(type);
            List<Phyto> list1 = adapter1.fromJson(json);
            for (Phyto item : list1) {
                item.registered = true;
                item.used = false;
            }
            database.dao().insert(list1.toArray(new Phyto[list1.size()]));

            // Load species from Open Nomenclature
//            json = readJsonFromAssets(context, "lexicon/species.json");
//            type = Types.newParameterizedType(List.class, Specie.class);
//            JsonAdapter<List<Specie>> adapter2 = moshi.adapter(type);
//            List<Specie> list2 = adapter2.fromJson(json);
//            database.specieDAO().insert(list2.toArray(new Specie[list2.size()]));

            // Load seeds from Lexicon
            json = readJsonFromAssets(context, "lexicon/seeds.json");
            type = Types.newParameterizedType(List.class, Seed.class);
            JsonAdapter<List<Seed>> adapter3 = moshi.adapter(type);
            List<Seed> list3 = adapter3.fromJson(json);
            for (Seed item : list3) {
                item.registered = true;
                item.used = false;
            }
            database.dao().insert(list3.toArray(new Seed[list3.size()]));

            // Load fertilizers from Lexicon
            json = readJsonFromAssets(context, "lexicon/fertilizers.json");
            type = Types.newParameterizedType(List.class, Fertilizer.class);
            JsonAdapter<List<Fertilizer>> adapter4 = moshi.adapter(type);
            List<Fertilizer> list4 = adapter4.fromJson(json);
            for (Fertilizer item : list4) {
                item.registered = true;
                item.used = false;
            }
            database.dao().insert(list4.toArray(new Fertilizer[list4.size()]));


            database.dao().insert(new Plot("1","La Renambrie",null,3.4f,
                    null,null,null,null));



            database.dao().insert(new Plot("2", "Myrmidon", null, 2.3f,
                    null, null, null, null));

            database.dao().insert(new Subplot("1", null, 1.4f, null,
                    null, null, "2", null));

            database.dao().insert(new Subplot("2", null, .9f, null,
                    null, null, "2", null));



            database.dao().insert(new Plot("3", "Les Grands Pièces", null, 5.6f,
                    null, null, null, null));



            database.dao().insert(new Crop("1", "Blé tendre de printemps 2018",
                    null,null,null,null,
                    null,null,3.4f,null,null,null,"1",null,null));

            database.dao().insert(new Crop("2", "Orge de printemps 2018",
                    null,null,null,null,
                    null,null,1.4f,null,null,null,"2", "1", null));

            database.dao().insert(new Crop("3", "Blé tendre de printemps 2018",
                    null,null,null,null,
                    null,null,.9f,null,null,null,"2","2",null));

            database.dao().insert(new Crop("4", "Maïs 2018",
                    null,null,null,null,
                    null,null,5.6f,null,null,null,"3",null,null));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String readJsonFromAssets(Context context, String fileName) {

        try {

            InputStream inputStream = context.getAssets().open(fileName);
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();

            if (bytesRead != buffer.length) Log.e(TAG, "Error while reading file %s" + fileName);

            return new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}


//        if (plantDAO().count() == 0) {
//            Plant plant = new Plant();
//            beginTransaction();
//            try {
//                plant.ek_id = 1043;
//                plant.variety = "Culture d’orge de printemps";
//                plant.variety = "hordeum_distichum";
//                plant.user = "remidechazelles@gmail.com - https://zero.ekylibre-test.farm";
//                plantDAO().insert(plant);
//                setTransactionSuccessful();
//            } finally {
//                endTransaction();
//            }
//        }

//    private static AppDatabase create(final Context context) {
//        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
//    }

    // Manage migrations
//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            // Since we didn't alter the table, there's nothing else to do here.
//        }
//    };


