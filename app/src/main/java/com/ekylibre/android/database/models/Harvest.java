package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.ekylibre.android.utils.SpinnerLists;
import com.ekylibre.android.utils.Units;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = Harvest.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Storage.class,
                parentColumns = Storage.COLUMN_ID,
                childColumns = Harvest.COLUMN_STORAGE_ID,
                onDelete = CASCADE)
)
public class Harvest {

    public static final String TABLE_NAME = "harvests";
    public static final String COLUMN_ID = "harvest_id";
    public static final String COLUMN_STORAGE_ID = "id_storage";
    public static final String COLUMN_INTERVENTION_ID = "id_intervention";


    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_INTERVENTION_ID, index = true)
    public Integer intervention_id;

    @ColumnInfo(name = COLUMN_STORAGE_ID, index = true)
    public Integer id_storage;

    public Float quantity;
    public String unit;
    public String number;
    public String type;

    public Harvest(Integer intervention_id, float quantity, String unit, Integer id_storage, String number, String type) {
        this.intervention_id = intervention_id;
        this.quantity = quantity;
        this.unit = unit;
        this.id_storage = id_storage;
        this.number = number;
        this.type = type;
    }

    @Ignore
    public Harvest() {
        this.unit = Units.QUINTAL_PER_HECTARE.toString();
        this.type = SpinnerLists.OUTPUT_LIST.get(0);
    }
}