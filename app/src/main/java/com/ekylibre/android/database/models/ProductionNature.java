package com.ekylibre.android.database.models;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.provider.BaseColumns;


@Entity(tableName = ProductionNature.TABLE_NAME)
public class ProductionNature {

    public static final String TABLE_NAME = "lexicon_production_natures";

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_SPECIE = "specie";
    public static final String COLUMN_HUMAN_NAME_FRA = "human_name_fra";
    public static final String COLUMN_AGROEDI_CROP_CODE = "agroedi_crop_code";
    public static final String COLUMN_SEASON = "season";
    public static final String COLUMN_PFI_CROP_CODE = "pfi_crop_code";

    @ColumnInfo(name = COLUMN_ID)
    @PrimaryKey
    public Integer id;

    @ColumnInfo(name = COLUMN_SPECIE, index = true)
    public String specie;

    @ColumnInfo(name = COLUMN_HUMAN_NAME_FRA, index = true)
    public String human_name_fra;

    @ColumnInfo(name = COLUMN_AGROEDI_CROP_CODE)
    public String agroedi_crop_code;

    @ColumnInfo(name = COLUMN_SEASON)
    public String season;

    @ColumnInfo(name = COLUMN_PFI_CROP_CODE)
    public String pfi_crop_code;

    public ProductionNature(String specie, String human_name_fra, String agroedi_crop_code,
                            String season, String pfi_crop_code) {
        this.specie = specie;
        this.human_name_fra = human_name_fra;
        this.agroedi_crop_code = agroedi_crop_code;
        this.season = season;
        this.pfi_crop_code = pfi_crop_code;
    }

}

//    public static final String COLUMN_HUMAN_NAME = "human_name";
//    public static final String COLUMN_STARTED_ON = "started_on";
//    public static final String COLUMN_STOPPED_ON = "stopped_on";

//    @ColumnInfo(variety = COLUMN_HUMAN_NAME)
//    public String human_name;

//    @ColumnInfo(variety = COLUMN_STARTED_ON)
//    public String started_on;

//    @ColumnInfo(variety = COLUMN_STOPPED_ON)
//    public String stopped_on;