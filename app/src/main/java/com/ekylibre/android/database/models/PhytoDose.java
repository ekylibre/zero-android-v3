package com.ekylibre.android.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.Nullable;


@Entity(tableName = PhytoDose.TABLE_NAME)
public class PhytoDose {

    public static final String TABLE_NAME = "phyto_doses";
    public static final String COLUMN_PRODUCT_ID = "product_id";

    @PrimaryKey
    @ColumnInfo(name = COLUMN_PRODUCT_ID)
    public Integer product_id;

    @Nullable
    public Float dose;

    public PhytoDose(Integer product_id, @Nullable Float dose) {
        this.product_id = product_id;
        this.dose = dose;
    }
}