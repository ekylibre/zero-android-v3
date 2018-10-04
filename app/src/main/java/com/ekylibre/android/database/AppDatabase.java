package com.ekylibre.android.database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.ekylibre.android.database.converters.DateConverter;
import com.ekylibre.android.database.converters.PolygonConverter;
import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.EquipmentType;
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
            Equipment.class, InterventionEquipment.class, EquipmentType.class,
            Person.class, InterventionPerson.class,
            Weather.class,
            Harvest.class, Storage.class,
            Crop.class, InterventionCrop.class, Plot.class,
            Point.class
        },
        exportSchema = false,
        version = 8
)
@TypeConverters({DateConverter.class, PolygonConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    // Entities tables
    public abstract DAO dao();

    // Database instance
    private static AppDatabase database;

    private static Context context;

    public static synchronized AppDatabase getInstance(Context ctx) {
        context = ctx;
        if (database == null)
            database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,"db")
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,
                            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .allowMainThreadQueries()
                    .build();
        return database;
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

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            Cursor cursor = database.query("SELECT farm_id FROM farms LIMIT 1");
            cursor.moveToFirst();
            String farmId = cursor.getString(cursor.getColumnIndex("farm_id"));

            database.execSQL("CREATE TABLE temp_storages (storage_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, type TEXT, storage_id_eky INTEGER, farm TEXT)");

            database.execSQL("INSERT INTO temp_storages (name, type, storage_id_eky, farm)" +
                    "SELECT name, type, storage_id, '" + farmId + "' FROM storages");

            database.execSQL("DROP TABLE storages");

            database.execSQL("ALTER TABLE temp_storages RENAME TO storages");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            Timber.e("MIGRATION_6_7");

            // Create new table
            database.execSQL(
                    "CREATE TABLE equipment_types (id INTEGER PRIMARY KEY, name TEXT, " +
                            "field_1_name TEXT, field_1_unit TEXT, field_2_name TEXT, " +
                            "field_2_unit TEXT, type TEXT)"
            );

            // Load Equipment types and more from Lexicon
            Moshi moshi = new Moshi.Builder().build();
            Type type = Types.newParameterizedType(List.class, EquipmentType.class);
            JsonAdapter<List<EquipmentType>> equipmentAdapter = moshi.adapter(type);
            String json;

            // Load and insert data into table
            try {
                InputStream inputStream = context.getAssets().open("lexicon/equipments.json");
                byte[] buffer = new byte[inputStream.available()];
                int bytesRead = inputStream.read(buffer);
                inputStream.close();

                if (bytesRead != buffer.length) Timber.e("Error while reading file");

                json = new String(buffer, "UTF-8");

                List<EquipmentType> equipmentTypes = equipmentAdapter.fromJson(json);
                if (equipmentTypes != null)
                    for (EquipmentType equipmentType : equipmentTypes) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("id", equipmentType.id);
                        contentValues.put("name", equipmentType.name);
                        contentValues.put("field_1_name", equipmentType.field_1_name);
                        contentValues.put("field_1_unit", equipmentType.field_1_unit);
                        contentValues.put("field_2_name", equipmentType.field_2_name);
                        contentValues.put("field_2_unit", equipmentType.field_2_unit);
                        contentValues.put("type", equipmentType.type);
                        database.insert("equipment_types", SQLiteDatabase.CONFLICT_IGNORE, contentValues);
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("UPDATE equipments SET number = NULL WHERE number = '';");
            database.execSQL("ALTER TABLE equipments ADD COLUMN field1Value TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE equipments ADD COLUMN field2Value TEXT DEFAULT NULL");
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
            JsonAdapter<List<Phyto>> phytoAdapter = moshi.adapter(type);
            if (json != null) {
                List<Phyto> phytos = phytoAdapter.fromJson(json);
                if (phytos != null) {
                    for (Phyto item : phytos) {
                        item.registered = true;
                        item.used = false;
                        item.unit = "LITER";
                    }
                    database.dao().insert(phytos.toArray(new Phyto[0]));
                }
            }

            // Load fertilizers from Lexicon
            json = readJsonFromAssets(context, "lexicon/fertilizers.json");
            type = Types.newParameterizedType(List.class, Fertilizer.class);
            JsonAdapter<List<Fertilizer>> fertilizerAdapter = moshi.adapter(type);
            if (json != null) {
                List<Fertilizer> fertilizers = fertilizerAdapter.fromJson(json);
                if (fertilizers != null) {
                    for (Fertilizer item : fertilizers) {
                        item.registered = true;
                        item.used = false;
                        item.unit = "KILOGRAM";
                    }
                    database.dao().insert(fertilizers.toArray(new Fertilizer[0]));
                }
            }

            // Load phytosanitary max dose usage from Lexicon
            json = readJsonFromAssets(context, "lexicon/phytosanitary_doses.json");
            type = Types.newParameterizedType(List.class, PhytoDose.class);
            JsonAdapter<List<PhytoDose>> dosesAdapter = moshi.adapter(type);
            if (json != null) {
                List<PhytoDose> doses = dosesAdapter.fromJson(json);
                if (doses != null)
                    database.dao().insert(doses.toArray(new PhytoDose[0]));
            }

            // Load seeds from Lexicon
            type = Types.newParameterizedType(List.class, Seed.class);
            json = readJsonFromAssets(context, "lexicon/seeds.json");
            JsonAdapter<List<Seed>> seedAdapter = moshi.adapter(type);
            if (json != null) {
                List<Seed> seeds = seedAdapter.fromJson(json);
                if (seeds != null) {
                    for (Seed seed : seeds) {
                        seed.registered = true;
                        seed.used = false;
                        seed.unit = "KILOGRAM";
                    }
                    database.dao().insert(seeds.toArray(new Seed[0]));
                }
            }

            // Load seeds from Lexicon
            type = Types.newParameterizedType(List.class, EquipmentType.class);
            json = readJsonFromAssets(context, "lexicon/equipments.json");
            JsonAdapter<List<EquipmentType>> equipmentAdapter = moshi.adapter(type);
            if (json != null) {
                List<EquipmentType> equipmentTypes = equipmentAdapter.fromJson(json);
                if (equipmentTypes != null)
                    database.dao().insert(equipmentTypes.toArray(new EquipmentType[0]));
            }

            Timber.e("Reference data inserted !");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String readJsonFromAssets(Context context, String fileName) {

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