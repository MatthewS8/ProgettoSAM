package com.github.matthews8.placeswishlist.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.android.libraries.places.api.model.Place

@Entity(
    tableName = "place_table",
    foreignKeys = [
        ForeignKey(
            entity = City::class,
            parentColumns = arrayOf("cityId"),
            childColumns = arrayOf("cityId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION)]
)
data class Place(
    @PrimaryKey(autoGenerate = true)
    var placeId: Long = 0L,

    @ColumnInfo(name = "gPlaceId")
    var place_id: String? = null,

    @ColumnInfo
    var name: String,

    @ColumnInfo
    var address: String,

    @ColumnInfo
    var visited: Boolean = false,

    @ColumnInfo
    var type: Place.Type? = null,

    @ColumnInfo(name = "icon")
    var iconUrl: String? = null,

    @ColumnInfo (index = true)
    var cityId: Long,

)
