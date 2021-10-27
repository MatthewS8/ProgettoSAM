package com.github.matthews8.placeswishlist.database

import androidx.room.*
import com.google.android.gms.common.util.Hex
import com.google.android.gms.maps.model.LatLng

@Entity(
    tableName = "city_table",
    indices = [Index(value = ["name", "country"],
        unique = true)],
)
data class City(
    @PrimaryKey(autoGenerate = true)
    var cityId: Long = 0L,

    @ColumnInfo(name = "latitude")
    var lat: Double,

    @ColumnInfo(name = "longitude")
    var lng: Double,

    @ColumnInfo
    var name: String,

    @ColumnInfo
    var country: String,

    @ColumnInfo
    var visited: Boolean = false,

)