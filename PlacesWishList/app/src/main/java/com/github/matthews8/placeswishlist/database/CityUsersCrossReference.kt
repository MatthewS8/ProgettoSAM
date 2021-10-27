package com.github.matthews8.placeswishlist.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "city_user_cross_table",
    primaryKeys = ["cityId", "username"],
)
data class CityUsersCrossReference(
    val cityId: Long,
    val username: String,
)

