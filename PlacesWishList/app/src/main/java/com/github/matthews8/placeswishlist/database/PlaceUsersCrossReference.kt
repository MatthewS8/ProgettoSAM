package com.github.matthews8.placeswishlist.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "place_user_cross_table",
    primaryKeys = ["placeId", "username"],
    foreignKeys = [
        ForeignKey(
            entity = Place::class,
            parentColumns = arrayOf("placeId"),
            childColumns = arrayOf("placeId"),
            onDelete = ForeignKey.CASCADE, //TODO  <-- TESTARE COSA SUCCEDE PERCHE POTREBBE CANCELLARE TUTTO
            onUpdate = ForeignKey.CASCADE),
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("username"),
            childColumns = arrayOf("username"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE)]
)
data class PlaceUsersCrossReference(
    val placeId: Long,
    val username: String,
)
