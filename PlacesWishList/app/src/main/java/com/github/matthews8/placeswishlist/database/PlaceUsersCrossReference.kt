package com.github.matthews8.placeswishlist.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "place_user_cross_table",
    primaryKeys = ["placeId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Place::class,
            parentColumns = arrayOf("placeId"),
            childColumns = arrayOf("placeId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("userId"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE)]
)
data class PlaceUsersCrossReference(
    val placeId: Long,
    val userId: Long,
)
