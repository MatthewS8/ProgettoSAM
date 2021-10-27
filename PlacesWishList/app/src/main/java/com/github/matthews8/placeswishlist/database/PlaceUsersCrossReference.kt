package com.github.matthews8.placeswishlist.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "place_user_cross_table",
    primaryKeys = ["placeId", "username"],
)
data class PlaceUsersCrossReference(
    val placeId: Long,
    val username: String,
)
