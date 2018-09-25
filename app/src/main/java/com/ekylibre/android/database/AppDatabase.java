package com.ekylibre.android.database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import android.content.Context;
import androidx.annotation.NonNull;

import com.ekylibre.android.database.converters.DateConverter;
import com.ekylibre.android.database.converters.PolygonConverter;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Harvest;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.PhytoDose;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Point;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Storage;
import com.ekylibre.android.database.models.Weather;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionMaterial;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import timber.log.Timber;


@Database(
        entities = {
            Farm.class, Intervention.class,
            InterventionWorkingDay.class,
            Phyto.class, InterventionPhytosanitary.class, PhytoDose.class,
            Seed.class, InterventionSeed.class,
            Fertilizer.class, InterventionFertilizer.class,
            Material.class, InterventionMaterial.class,
            Equipment.class, InterventionEquipment.class,
            Person.class, InterventionPerson.class,
            Weather.class,
            Harvest.class, Storage.class,
            Crop.class, InterventionCrop.class, Plot.class,
            Point.class
        },
        exportSchema = false,
        version = 5
)
@TypeConverters(
        { DateConverter.class, PolygonConverter.class }
)
public abstract class AppDatabase extends RoomDatabase {

    // Entities tables
    public abstract DAO dao();

    // Database instance
    private static AppDatabase database;

    public static synchronized AppDatabase getInstance(Context context) {
        if (database == null)
            database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,"db")
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5)
                    .build();
        return database;
    }

    public static synchronized void revokeInstance() {
        database = null;
    }


    /**
     * Manage migrations
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE points (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "time INTEGER NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, " +
                    "speed REAL NOT NULL, accuracy INTEGER NOT NULL, type TEXT, " +
                    "intervention_id INTEGER NOT NULL)"
            );
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE temp_interwd (wd_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "wd_intervention_id INTEGER NOT NULL, hour_duration REAL NOT NULL, " +
                    "execution_date TEXT NOT NULL, " +
                    "FOREIGN KEY(wd_intervention_id) REFERENCES interventions(intervention_id) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )");

            database.execSQL("INSERT INTO temp_interwd (wd_id, wd_intervention_id, hour_duration, execution_date) "
                    + "SELECT wd_id, wd_intervention_id, CAST(hour_duration AS REAL), execution_date "
                    + "FROM intervention_working_days");

            database.execSQL("DROP TABLE intervention_working_days");

            database.execSQL("ALTER TABLE temp_interwd RENAME TO intervention_working_days");

            database.execSQL("CREATE INDEX index_intervention_working_days_wd_id ON intervention_working_days (wd_id)");
            database.execSQL("CREATE INDEX index_intervention_working_days_wd_intervention_id ON intervention_working_days (wd_intervention_id)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE materials ADD COLUMN material_id_eky INTEGER DEFAULT NULL");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE temp_crops (crop_uuid TEXT NOT NULL, name TEXT, specie TEXT, " +
                        "production_nature TEXT, production_mode TEXT, production_output TEXT, " +
                        "provisional_yield TEXT, shape TEXT, surface_area REAL NOT NULL, " +
                        "centroid TEXT, start_date TEXT, stop_date TEXT, plot_uuid TEXT, farm TEXT, " +
                        "PRIMARY KEY(crop_uuid))");

        database.execSQL("INSERT INTO temp_crops (crop_uuid, name, specie, production_nature, " +
                "production_mode, production_output, provisional_yield, shape, surface_area, " +
                "centroid, start_date, stop_date, plot_uuid, farm) " +
                "SELECT crop_uuid, name, specie, " +
                "production_nature, production_mode, production_output, provisional_yield, shape," +
                "surface_area, centroid, start_date, stop_date, plot, farm FROM crops");

        database.execSQL("DROP TABLE crops;");

        database.execSQL("ALTER TABLE temp_crops RENAME TO crops");

        database.execSQL("CREATE INDEX index_crops_farm ON crops (farm)");
        database.execSQL("CREATE INDEX index_crops_name ON crops (name)");
        }
};


    /**
     * Populate database with initial Lexicon data
     */
    public void populateInitialData(Context context) {

        try {

            String json;
            database = AppDatabase.getInstance(context);
            Moshi moshi = new Moshi.Builder().build();


            // Load phytosanitary products from Lexicon
            json = readJsonFromAssets(context, "lexicon/phytosanitary_products.json");
            Type type = Types.newParameterizedType(List.class, Phyto.class);
            JsonAdapter<List<Phyto>> adapter1 = moshi.adapter(type);
            List<Phyto> list1 = adapter1.fromJson(json);
            for (Phyto item : list1) {
                item.registered = true;
                item.used = false;
                item.unit = "LITER";
            }
            database.dao().insert(list1.toArray(new Phyto[list1.size()]));

            // Load fertilizers from Lexicon
            json = readJsonFromAssets(context, "lexicon/fertilizers.json");
            type = Types.newParameterizedType(List.class, Fertilizer.class);
            JsonAdapter<List<Fertilizer>> adapter4 = moshi.adapter(type);
            List<Fertilizer> list4 = adapter4.fromJson(json);
            for (Fertilizer item : list4) {
                item.registered = true;
                item.used = false;
                item.unit = "KILOGRAM";
            }
            database.dao().insert(list4.toArray(new Fertilizer[list4.size()]));

            // Load phytosanitary max dose usage from Lexicon
            json = readJsonFromAssets(context, "lexicon/phytosanitary_doses.json");
            type = Types.newParameterizedType(List.class, PhytoDose.class);
            JsonAdapter<List<PhytoDose>> adapter5 = moshi.adapter(type);
            List<PhytoDose> list5 = adapter5.fromJson(json);
            database.dao().insert(list5.toArray(new PhytoDose[list5.size()]));

            // Load species from Open Nomenclature
//            json = readJsonFromAssets(context, "lexicon/species.json");
//            nature = Types.newParameterizedType(List.class, Specie.class);
//            JsonAdapter<List<Specie>> adapter2 = moshi.adapter(nature);
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
                item.unit = "KILOGRAM";
            }
            database.dao().insert(list3.toArray(new Seed[list3.size()]));


            Timber.e("Reference data inserted !");

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

            if (bytesRead != buffer.length) Timber.e("Error while reading file %s", fileName);

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




