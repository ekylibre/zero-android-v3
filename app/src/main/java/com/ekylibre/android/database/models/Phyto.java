package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = Phyto.TABLE_NAME)
public class Phyto {

    public static final String TABLE_NAME = "phytos";
    public static final String COLUMN_ID = "phyto_id";
    public static final String COLUMN_ID_EKY = COLUMN_ID + "_eky";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    public Integer id;

    @ColumnInfo(name = COLUMN_ID_EKY)
    public Integer eky_id;

    @ColumnInfo(index = true)
    public String name;

    public String nature;

    @ColumnInfo(index = true)
    public String maaid;

    public Integer mix_category_code;

    public Integer in_field_reentry_delay;

    public String firm_name;

    @ColumnInfo(index = true)
    public Boolean registered;

    @ColumnInfo(index = true)
    public Boolean used;

    public String unit;

    @Ignore
    public Float dose_max;

    public Phyto(Integer id, Integer eky_id, String name, String nature, String maaid, Integer mix_category_code,
                 Integer in_field_reentry_delay, String firm_name, Boolean registered, Boolean used, String unit) {
        this.id = id;
        this.eky_id = eky_id;
        this.name = name;
        this.nature = nature;
        this.maaid = maaid;
        this.mix_category_code = mix_category_code;
        this.in_field_reentry_delay = in_field_reentry_delay;
        this.firm_name = firm_name;
        this.registered = registered;
        this.used = used;
        this.unit = unit;
    }

}