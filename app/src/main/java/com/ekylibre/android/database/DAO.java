package com.ekylibre.android.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.ekylibre.android.database.models.Crop;
import com.ekylibre.android.database.models.Equipment;
import com.ekylibre.android.database.models.Farm;
import com.ekylibre.android.database.models.Fertilizer;
import com.ekylibre.android.database.models.Intervention;
import com.ekylibre.android.database.models.Material;
import com.ekylibre.android.database.models.Person;
import com.ekylibre.android.database.models.Phyto;
import com.ekylibre.android.database.models.Plot;
import com.ekylibre.android.database.models.Seed;
import com.ekylibre.android.database.models.Specie;
import com.ekylibre.android.database.models.Subplot;
import com.ekylibre.android.database.pojos.Interventions;
import com.ekylibre.android.database.relations.InterventionCrop;
import com.ekylibre.android.database.relations.InterventionEquipment;
import com.ekylibre.android.database.relations.InterventionFertilizer;
import com.ekylibre.android.database.relations.InterventionMaterial;
import com.ekylibre.android.database.relations.InterventionPerson;
import com.ekylibre.android.database.relations.InterventionPhytosanitary;
import com.ekylibre.android.database.relations.InterventionSeed;

import java.util.List;


@Dao
public interface DAO {

    /**
     *    Insert queries
     */


//    @Insert long insert(Farm... farms);
//    @Insert void insert(ProductionNature... productionNatures);

    @Insert void insert(Equipment... equipments);
    @Insert void insert(Fertilizer... fertilizers);
    @Insert void insert(Material... materials);
    @Insert void insert(Person... persons);
    @Insert void insert(Phyto... phytos);
    @Insert void insert(Seed... seeds);
    @Insert void insert(Specie... species);

    @Insert long insert(Intervention intervention);

    @Insert void insert(InterventionSeed interventionSeeds);
    @Insert void insert(InterventionPhytosanitary interventionPhytosanitary);
    @Insert void insert(InterventionFertilizer interventionFertilizers);
    @Insert void insert(InterventionMaterial interventionMaterial);
    @Insert void insert(InterventionEquipment interventionEquipments);
    @Insert void insert(InterventionPerson interventionPerson);
    @Insert void insert(InterventionCrop interventionCrop);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Plot... plots);

    @Insert void insert(Subplot... subplots);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Crop... crops);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Farm... farms);

    /**
     *    Crops selection list
     */
//    @Transaction @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE start_date < :now AND stop_date > :now")
//    List<CropWithPlots> listCropWithPlots(long now);

//    @Transaction @Query("SELECT * FROM " + Plot.TABLE_NAME + " WHERE start_date < :now AND stop_date > :now")
//    List<PlotWithCrops> cropsByPlot(long now);

    @Query("SELECT * FROM " + Crop.TABLE_NAME + " WHERE plot = :uuid")  //AND start_date < :now AND stop_date > :now
    List<Crop> cropsByPlotUuid(String uuid);  //long now


    /**
     *    Plot list
     */
    @Query("SELECT * FROM " + Plot.TABLE_NAME + " ORDER BY name")
    List<Plot> plotList();


    /**
     *    Interventions (POJO)
     */

    @Transaction @Query("SELECT * FROM " + Intervention.TABLE_NAME + " ORDER BY date DESC")
    List<Interventions> selectInterventions();


    /**
     *    Equipment
     */

    @Query("SELECT * FROM " + Equipment.TABLE_NAME + " ORDER BY name")
    List<Equipment> selectEquipment();

    @Query("SELECT * FROM " + Equipment.TABLE_NAME + " WHERE name LIKE :search ORDER BY name" )
    List<Equipment> searchEquipment(String search);


    /**
     *    Material
     */

    @Query("SELECT * FROM " + Material.TABLE_NAME + " ORDER BY name")
    List<Material> selectMaterial();

    @Query("SELECT * FROM " + Material.TABLE_NAME + " WHERE name LIKE :search ORDER BY name" )
    List<Material> searchMaterial(String search);


    /**
     *    Seed
     */

    @Query("SELECT MAX(" + Seed.COLUMN_ID + ") FROM " + Seed.TABLE_NAME + " WHERE used = 1")
    Integer lastSeedId();

    @Transaction @Query("SELECT * FROM " + Seed.TABLE_NAME)
    List<Seed> selectSeed();

    @Transaction @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE variety LIKE :search" )
    List<Seed> searchSeedVariety(String search);

    @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE specie = :specie AND variety LIKE :search" )
    List<Seed> searchBySpecie(String specie, String search);

    @Query("SELECT * FROM " + Seed.TABLE_NAME + " WHERE specie = :specie" )
    List<Seed> selectBySpecie(String specie);

//    @Query("SELECT " + Seed.TABLE_NAME + ".*, " + Specie.TABLE_NAME + ".fra" + " FROM " + Seed.TABLE_NAME
//    + " INNER JOIN " + Specie.TABLE_NAME + " ON " + Specie.COLUMN_NAME + " = " + Seed.COLUMN_SPECIE)
//    List<Seed> selectInterventions();


    /**
     *    Phytosanitary
     **/

    @Query("SELECT MAX(" + Phyto.COLUMN_ID + ") FROM " + Phyto.TABLE_NAME + " WHERE used = 1")
    Integer lastPhytosanitaryId();

    @Transaction @Query("SELECT * FROM " + Phyto.TABLE_NAME + " ORDER BY used DESC")
    List<Phyto> selectPhytosanitary();


    @Query("SELECT * FROM " + Phyto.TABLE_NAME + " WHERE name LIKE :search ORDER BY name, used DESC")
    List<Phyto> searchPhytosanitary(String search);


    /**
     *    Person
     **/

    @Query("SELECT * FROM " + Person.TABLE_NAME + " ORDER BY first_name")
    List<Person> selectPerson();

    @Query("SELECT * FROM " + Person.TABLE_NAME + " WHERE first_name LIKE :search OR last_name LIKE :search ORDER BY first_name" )
    List<Person> searchPerson(String search);


    /**
     *    Fertilizer
     **/

    @Query("SELECT MAX(" + Fertilizer.COLUMN_ID + ") FROM " + Fertilizer.TABLE_NAME + " WHERE used = 1")
    Integer lastFertilizerId();

    @Transaction @Query("SELECT * FROM " + Fertilizer.TABLE_NAME + " ORDER BY used DESC, label_fra")
    List<Fertilizer> selectFertilizer();

    @Transaction @Query("SELECT * FROM " + Fertilizer.TABLE_NAME + " WHERE label_fra LIKE :search ORDER BY label_fra" )
    List<Fertilizer> searchFertilizer(String search);

    /**
     *    Crop
     */

    @Query("SELECT * FROM " + Crop.TABLE_NAME + " ORDER BY name")
    List<Crop> selectCrop();

    /**
     *    ProductionNature
     **/

//    @Query("SELECT * FROM " + ProductionNature.TABLE_NAME + " ORDER BY " + ProductionNature.COLUMN_HUMAN_NAME_FRA)
//    List<ProductionNature> selectProductionNature();
//
//    @Query("SELECT * FROM " + ProductionNature.TABLE_NAME + " WHERE " + ProductionNature.COLUMN_HUMAN_NAME_FRA + " LIKE :search ORDER BY " + ProductionNature.COLUMN_HUMAN_NAME_FRA)
//    List<ProductionNature> searchProductionNature(String search);



}

