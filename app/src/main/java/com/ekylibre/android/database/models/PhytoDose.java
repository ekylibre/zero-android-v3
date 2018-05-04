package com.ekylibre.android.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;


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