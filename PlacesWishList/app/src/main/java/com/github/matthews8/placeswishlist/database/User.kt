package com.github.matthews8.placeswishlist.database

import androidx.room.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    var userId: Long = 0L,

    @ColumnInfo
    var username: String = "owner",

    @ColumnInfo
    var color_picked: Float = BitmapDescriptorFactory.HUE_RED,
)
