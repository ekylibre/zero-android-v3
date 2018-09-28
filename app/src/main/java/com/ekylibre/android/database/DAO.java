package com.ekylibre.android.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ekylibre.android.InterventionActivity;
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
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.pojos.SimpleInterventions;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionMaterial;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;
import com.ekylibre.android.database.relations.InterventionWorkingDay;

import java.util.List;


@Dao
public interface DAO {

    /**
     *    Insert queries
     */
    @Insert void insert(Phyto... phytos);
    @Insert void insert(PhytoDose... doses);
    @Insert void insert(Seed... seeds);
    @Insert void insert(Point... points);

    @Insert(onConflict = OnConflictStrategy.REPLACE) long insert(Intervention intervention);  // return id as long

    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Weather... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Storage... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Person... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Plot... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Crop... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Farm... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) long insert(Equipment item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Material... items);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Fertilizer... items);
//    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(EquipmentType... items);

    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionWorkingDay item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionSeed item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionPhytosanitary item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionFertilizer item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionMaterial item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionEquipment item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionPerson item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(InterventionCrop item);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void insert(Harvest item);

    @Delete void delete(InterventionWorkingDay... items);
    @Delete void delete(InterventionSeed... items);
    @Delete void delete(InterventionPhytosanitary... items);
    @Delete void delete(InterventionFertilizer... items);
    @Delete void delete(InterventionMaterial... items);
    @Delete void delete(InterventionEquipment... items);
    @Delete void delete(InterventionPerson... items);
    @Delete void delete(InterventionCrop... items);
    @Delete void delete(Weather... items);
    @Delete void delete(Harvest... items);
    @Delete void delete(Equipment... items);

    @Delete void delete(Intervention intervention);

    /**
     *    Crops selection list
     */
//    @Transaction @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE start_date < :now AND stop_date > :now")
//    List<CropWithPlots> listCropWithPlots(long now);

//    @Transaction @Query("SELECT * FROM " + Plot.TABLE_NAME + " WHERE start_date < :now AND stop_date > :now")
//    List<PlotWithCrops> cropsByPlot(long now);

//    @Query("SELECT * FROM " +Crop.TABLE_NAME+
//            " JOIN " +Plot.TABLE_NAME+ " ON " +Plot.COLUMN_UUID+ " = " +Crop.COLUMN_PLOT+ " WHERE plot_uuid = :uuid")  //AND start_date < :now AND stop_date > :now
//    List<Crop> cropsByPlotUuid(String uuid);  //long now

//    @Query("SELECT DISTINCT production_nature FROM " + Crop.TABLE_NAME + " WHERE farm = :farmId")
//    List<String> getProductions(String farmId);

//    @Transaction
//    @Query("SELECT DISTINCT crop_uuid, i.intervention_id, c.*, i.* FROM " + Crop.TABLE_NAME +" AS c, " + Intervention.TABLE_NAME + " AS i" +
//            " JOIN " + InterventionCrop.TABLE_NAME + " ON " + Crop.COLUMN_UUID + " = " + InterventionCrop.COLUMN_CROP_ID +
//            " JOIN " + Intervention.TABLE_NAME + " ON " + InterventionCrop.COLUMN_INTERVENTION_ID + " = " + Intervention.COLUMN_ID)
//    List<SimpleInterventions> getSimpleInterventionList(String farmId);

//    @Transaction
//    @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE farm = :farmId")
//    List<Crops> interCropList(String farmId);

//    @Transaction
//    @Query("SELECT * FROM " + InterventionCrop.TABLE_NAME +
//    " JOIN " + Crop.TABLE_NAME + " ON " + Crop.COLUMN_UUID + " = " + InterventionCrop.COLUMN_CROP_ID +
//    " JOIN " + Intervention.TABLE_NAME + " ON " + Intervention.COLUMN_ID + " = " + InterventionCrop.COLUMN_INTERVENTION_ID)
//    List<InterventionCropPojo> cropWithInter();
//
//    @Transaction
//    @Query("SELECT intervention_id, crop_uuid FROM " + Crop.TABLE_NAME +
//            " JOIN " + InterventionCrop.COLUMN_CROP_ID + " ON " + Crop.COLUMN_UUID + " = " + InterventionCrop.COLUMN_CROP_ID +
//            " JOIN " + Intervention.TABLE_NAME + " ON " + InterventionCrop.COLUMN_INTERVENTION_ID + " = " + Intervention.COLUMN_ID +
//            " WHERE farm = :farmId")
//    List<CropsWithIntervention> cropsWithInter(String farmId);

//    @Transaction
//    @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE farm = :farmId")
//    List<CropsWithIntervention> interventionCropDetail(String farmId);


    /**
     *    Plot list
     */
//    @Transaction
//    @Query("SELECT * FROM " + Plot.TABLE_NAME + " WHERE farm = :farmId ORDER BY name")
//    List<Plots> plotList(String farmId);

    @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE farm = :farmId ORDER BY name")
    List<Crop> cropList(String farmId);

    @Query("SELECT * FROM " + InterventionCrop.TABLE_NAME +
            " JOIN " + Crop.TABLE_NAME + " ON " + InterventionCrop.COLUMN_CROP_ID + " = " + Crop.COLUMN_UUID +
            " WHERE " + InterventionCrop.COLUMN_INTERVENTION_ID + " = :id")
    List<Crop> cropListForIntervention(int id);



    /**
     *    Interventions (POJO)
     */
    @Transaction
    @Query("SELECT i.* FROM " + Intervention.TABLE_NAME + " AS i " + " JOIN " + InterventionWorkingDay.TABLE_NAME +
            " ON " + InterventionWorkingDay.COLUMN_INTERVENTION_ID + " = " + Intervention.COLUMN_ID +
            " WHERE status != 'deleted' AND farm = :farmId ORDER BY execution_date DESC, intervention_id_eky DESC")
    List<Interventions> selectInterventions(String farmId);

    @Transaction
    @Query("SELECT i.* FROM " + Intervention.TABLE_NAME + " AS i " +
            " JOIN " + InterventionWorkingDay.TABLE_NAME + " ON " + InterventionWorkingDay.COLUMN_INTERVENTION_ID + " = " + Intervention.COLUMN_ID +
            " WHERE " + Intervention.COLUMN_ID + " IN (:interIDs)" +
            " ORDER BY execution_date DESC")
    List<Interventions> selectInterventionsByInterIDs(List<Integer> interIDs);

    @Transaction
    @Query("SELECT i.* FROM " + Intervention.TABLE_NAME + " AS i " +
            " JOIN " + InterventionWorkingDay.TABLE_NAME + " ON " + InterventionWorkingDay.COLUMN_INTERVENTION_ID + " = " + Intervention.COLUMN_ID +
            " WHERE farm = :farmId ORDER BY execution_date ASC")
    List<SimpleInterventions> getSimpleInterventionList(String farmId);

    @Transaction
    @Query("SELECT * FROM " + Intervention.TABLE_NAME + " WHERE " + Intervention.COLUMN_ID_EKY + " IS NULL AND status != 'deleted' AND farm = :farmId")
    List<Interventions> getSyncableInterventions(String farmId);

    @Transaction
    @Query("SELECT * FROM " + Intervention.TABLE_NAME + " WHERE " + Intervention.COLUMN_ID_EKY + " NOT NULL AND status = 'deleted' AND farm = :farmId")
    List<Interventions> getDeletableInterventions(String farmId);

    @Transaction
    @Query("SELECT * FROM " + Intervention.TABLE_NAME + " WHERE status = 'updated' AND " + Intervention.COLUMN_ID_EKY + " NOT NULL AND farm = :farmId")
    List<Interventions> getUpdatableInterventions(String farmId);

    @Query("UPDATE " + Intervention.TABLE_NAME + " SET " + Intervention.COLUMN_ID_EKY + " = :ekyId, " +
            "status = '" + InterventionActivity.SYNCED + "' WHERE " + Intervention.COLUMN_ID + " = :id")
    void setInterventionEkyId(int id, int ekyId);

    @Query("UPDATE " + Intervention.TABLE_NAME + " SET status = '" + InterventionActivity.SYNCED + "' WHERE " + Intervention.COLUMN_ID + " = :id")
    void setInterventionSynced(int id);

    @Query("UPDATE " + Intervention.TABLE_NAME + " SET status = :status WHERE " + Intervention.COLUMN_ID_EKY + " = :ekyId")
    void updateInterventionStatus(int ekyId, String status);

    @Query("DELETE FROM " + Intervention.TABLE_NAME + " WHERE " + Intervention.COLUMN_ID_EKY + " = :ekyId")
    void deleteIntervention(int ekyId);

    @Query("UPDATE " + Intervention.TABLE_NAME + " SET status = 'deleted' WHERE " + Intervention.COLUMN_ID + " = :id")
    void setDeleted(int id);

    @Transaction
    @Query("SELECT * FROM " + Intervention.TABLE_NAME + " WHERE " + Intervention.COLUMN_ID_EKY + " = :id")
    Interventions getIntervention(int id);


    /**
     * Ids lists
     */
    @Query("SELECT " + Intervention.COLUMN_ID_EKY + " FROM " + Intervention.TABLE_NAME + " WHERE " + Intervention.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> interventionsEkiIdList();

    @Query("SELECT " + Phyto.COLUMN_ID_EKY + " FROM " + Phyto.TABLE_NAME + " WHERE " + Phyto.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> phytoEkiIdList();

    @Query("SELECT " + Seed.COLUMN_ID_EKY + " FROM " + Seed.TABLE_NAME + " WHERE " + Seed.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> seedEkiIdList();

    @Query("SELECT " + Fertilizer.COLUMN_ID_EKY + " FROM " + Fertilizer.TABLE_NAME + " WHERE " + Fertilizer.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> fertilizerEkiIdList();

    @Query("SELECT " + Material.COLUMN_ID_EKY + " FROM " + Material.TABLE_NAME + " WHERE " + Material.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> materialEkiIdList();

    @Query("SELECT " + Person.COLUMN_ID_EKY + " FROM " + Person.TABLE_NAME + " WHERE " + Person.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> personEkiIdList();

    /**
     *    Storage
     */
    @Query("SELECT * FROM " + Storage.TABLE_NAME + " ORDER BY name")
    List<Storage> getStorages();


    /**
     *    Equipment
     */
    @Query("SELECT * FROM " + Equipment.TABLE_NAME + " ORDER BY name")
    List<Equipment> selectEquipment();

    @Query("SELECT * FROM " + Equipment.TABLE_NAME + " WHERE name LIKE :search ORDER BY name" )
    List<Equipment> searchEquipment(String search);

    @Query("UPDATE " + Equipment.TABLE_NAME + " SET " + Equipment.COLUMN_ID_EKY + " = :ekyId WHERE name LIKE :name")
    int setEquipmentEkyId(int ekyId, String name);

    @Query("SELECT " + Equipment.COLUMN_ID + " FROM " + Equipment.TABLE_NAME + " WHERE " + Equipment.COLUMN_ID_EKY + " = :eky_id")
    int getEquipmentId(int eky_id);

    @Query("SELECT * FROM "+Equipment.TABLE_NAME+" WHERE " + Equipment.COLUMN_ID_EKY + " IS NULL")
    List<Equipment> getEquipmentWithoutEkyId();

    @Query("SELECT name FROM " + Equipment.TABLE_NAME)
    List<String> selectEquipmentNames();


    /**
     *    Material
     */
    @Query("SELECT * FROM " + Material.TABLE_NAME + " ORDER BY name")
    List<Material> selectMaterial();

    @Query("SELECT * FROM " + Material.TABLE_NAME + " WHERE name LIKE :search ORDER BY name" )
    List<Material> searchMaterial(String search);

    @Query("SELECT * FROM " + Material.TABLE_NAME + " WHERE " + Material.COLUMN_ID_EKY + " IS NULL")
    List<Material> getMaterialWithoutEkyId();

    @Query("SELECT * FROM " + Material.TABLE_NAME + " WHERE " + Material.COLUMN_ID_EKY + " = :id")
    Material getMaterialByEkyId(Integer id);




    /**
     *    Seed
     */
    @Query("SELECT MAX(" + Seed.COLUMN_ID + ") FROM " + Seed.TABLE_NAME + " WHERE registered = 0")
    Integer lastSeedId();

    @Transaction @Query("SELECT * FROM " + Seed.TABLE_NAME)
    List<Seed> selectSeed();

    @Transaction @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE variety LIKE :search" )
    List<Seed> searchSeedVariety(String search);

    @Transaction @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE variety LIKE :search" )
    List<Seed> searchSeed(String search);

    @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE specie = :specie AND variety LIKE :search" )
    List<Seed> searchBySpecie(String specie, String search);

    @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE specie = :specie" )
    List<Seed> selectBySpecie(String specie);

//    @Query("UPDATE " + Seed.TABLE_NAME + " SET " + Seed.COLUMN_ID_EKY + " = :id WHERE " + Seed.COLUMN_ID + " = :refId")
//    int setSeedEkyId(Integer id, String refId);

    @Query("SELECT " + Seed.COLUMN_ID + " FROM " + Seed.TABLE_NAME + " WHERE " + Seed.COLUMN_ID_EKY + " = :eky_id")
    int getSeedId(int eky_id);

    @Query("UPDATE " + Seed.TABLE_NAME + " SET " + Seed.COLUMN_ID_EKY + " = :id WHERE " + Seed.COLUMN_ID + " = :refId")
    int setSeedEkyId(Integer id, String refId);

    @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE " + Seed.COLUMN_ID_EKY + " IS NULL AND CAST(" + Seed.COLUMN_ID + " AS INTEGER) < 100000")
    List<Seed> getSeedWithoutEkyId();

//    @Query("SELECT " + Seed.TABLE_NAME + ".*, " + Specie.TABLE_NAME + ".fra" + " FROM " + Seed.TABLE_NAME
//    + " INNER JOIN " + Specie.TABLE_NAME + " ON " + Specie.COLUMN_NAME + " = " + Seed.COLUMN_SPECIE)
//    List<Seed> selectInterventions();


    /**
     *    Phytosanitary
     **/
    @Query("SELECT MAX(" + Phyto.COLUMN_ID + ") FROM " + Phyto.TABLE_NAME + " WHERE registered = 0")
    Integer lastPhytosanitaryId();

    @Transaction @Query("SELECT * FROM " + Phyto.TABLE_NAME + " ORDER BY used DESC")
    List<Phyto> selectPhytosanitary();

    @Query("SELECT * FROM " + Phyto.TABLE_NAME + " WHERE name LIKE :search ORDER BY name, used DESC")
    List<Phyto> searchPhytosanitary(String search);

    @Query("SELECT " + Phyto.COLUMN_ID + " FROM " + Phyto.TABLE_NAME + " WHERE maaid = :refId")
    Integer phytoExists(String refId);

    @Query("UPDATE " + Phyto.TABLE_NAME + " SET " + Phyto.COLUMN_ID_EKY + " = :ekyId WHERE " + Phyto.COLUMN_ID + " = :id")  // AND name LIKE :name
    int setPhytoEkyId(int ekyId, int id);  // String name

    @Query("SELECT " + Phyto.COLUMN_ID + " FROM " + Phyto.TABLE_NAME + " WHERE " + Phyto.COLUMN_ID_EKY + " = :eky_id")
    int getPhytoId(int eky_id);

    @Query("SELECT dose FROM " + PhytoDose.TABLE_NAME + " WHERE product_id = :product_id")
    Float getMaxDose(int product_id);

    @Query("SELECT * FROM " + InterventionPhytosanitary.TABLE_NAME + " WHERE intervention_id = :inter_id AND phytosanitary_id = :phyto_id")
    InterventionPhytosanitary getPhytoInter(int inter_id, int phyto_id);

    @Query("SELECT * FROM " + Phyto.TABLE_NAME + " WHERE " + Phyto.COLUMN_ID_EKY + " IS NULL AND registered = 0")
    List<Phyto> getPhytoWithoutEkyId();


    /**
     *    Fertilizer
     **/
    @Query("SELECT MAX(" + Fertilizer.COLUMN_ID + ") FROM " + Fertilizer.TABLE_NAME + " WHERE registered = 0")
    Integer lastFertilizerId();

    @Transaction @Query("SELECT * FROM " + Fertilizer.TABLE_NAME + " ORDER BY used DESC, label_fra")
    List<Fertilizer> selectFertilizer();

    @Transaction @Query("SELECT * FROM " + Fertilizer.TABLE_NAME + " WHERE label_fra LIKE :search ORDER BY label_fra" )
    List<Fertilizer> searchFertilizer(String search);

    @Query("UPDATE " + Fertilizer.TABLE_NAME + " SET " + Fertilizer.COLUMN_ID_EKY + " = :id WHERE " + Fertilizer.COLUMN_ID + " = :refId")
    int setFertilizerEkyId(Integer id, String refId);

    @Query("SELECT " + Fertilizer.COLUMN_ID + " FROM " + Fertilizer.TABLE_NAME + " WHERE " + Fertilizer.COLUMN_ID_EKY + " = :eky_id")
    int getFertilizerId(int eky_id);

    @Query("SELECT * FROM " + Fertilizer.TABLE_NAME + " WHERE " + Fertilizer.COLUMN_ID_EKY + " IS NULL AND registered = 0")
    List<Fertilizer> getFertilizerWithoutEkyId();


    /**
     *    Person
     **/

    @Query("SELECT * FROM " + Person.TABLE_NAME + " ORDER BY first_name")
    List<Person> selectPerson();

    @Query("SELECT * FROM " + Person.TABLE_NAME + " WHERE first_name LIKE :search OR last_name LIKE :search ORDER BY first_name" )
    List<Person> searchPerson(String search);

    @Query("UPDATE " + Person.TABLE_NAME + " SET first_name = :firstName, last_name = :lastName WHERE " + Person.COLUMN_ID_EKY + " = :ekyId")
    void updatePerson(String firstName, String lastName, String ekyId);

    @Query("SELECT " + Person.COLUMN_ID + " FROM " + Person.TABLE_NAME + " WHERE " + Person.COLUMN_ID_EKY + " = :eky_id")
    int getPersonId(int eky_id);

    @Query("SELECT * FROM " + Person.TABLE_NAME + " WHERE " + Person.COLUMN_ID_EKY + " IS NULL")
    List<Person> getPersonsWithoutEkyId();

    @Query("UPDATE " + Person.TABLE_NAME + " SET " + Person.COLUMN_ID_EKY + " = :ekyId WHERE " + Person.COLUMN_ID + " = :id")
    void setPersonEkyId(int id, int ekyId);




    /**
     *    Crop
     */

    @Query("SELECT * FROM " + Crop.TABLE_NAME + " ORDER BY name")
    List<Crop> selectCrop();


    /**
     * Storages
     */
    @Query("UPDATE " + Storage.TABLE_NAME + " SET name = :name, type = :type WHERE " + Storage.COLUMN_ID_EKY + " = :ekyId")
    void updateStorage(String name, String type, int ekyId);

    @Query("SELECT " + Storage.COLUMN_ID + " FROM " + Storage.TABLE_NAME + " WHERE " + Storage.COLUMN_ID_EKY + " = :ekyId")
    int getStorageId(int ekyId);

    @Query("SELECT " + Storage.COLUMN_ID_EKY + " FROM " + Storage.TABLE_NAME + " WHERE " + Storage.COLUMN_ID_EKY + " NOT NULL")
    List<Integer> storageEkiIdList();

    @Query("SELECT " + Storage.COLUMN_ID_EKY + " FROM " + Storage.TABLE_NAME + " WHERE storage_id = :id")
    Integer getStorageEkiId(int id);

    @Query("SELECT * FROM " + Storage.TABLE_NAME + " WHERE " + Storage.COLUMN_ID_EKY + " IS NULL")
    List<Storage> getStoragesWithoutEkyId();

    @Query("UPDATE " + Storage.TABLE_NAME + " SET " + Storage.COLUMN_ID_EKY + " = :ekyId WHERE " + Storage.COLUMN_ID + " = :id")
    void setStorageEkyId(int id, int ekyId);



//    @Query("SELECT * FROM " + ProductionNature.TABLE_NAME + " ORDER BY " + ProductionNature.COLUMN_HUMAN_NAME_FRA)
//    List<ProductionNature> selectProductionNature();
//
//    @Query("SELECT * FROM " + ProductionNature.TABLE_NAME + " WHERE " + ProductionNature.COLUMN_HUMAN_NAME_FRA + " LIKE :search ORDER BY " + ProductionNature.COLUMN_HUMAN_NAME_FRA)
//    List<ProductionNature> searchProductionNature(String search);


    /**
     *    Polygon points
     */
    @Query("SELECT * FROM points ORDER BY id DESC LIMIT 1")
    LiveData<Point> getLastPoint();

}

