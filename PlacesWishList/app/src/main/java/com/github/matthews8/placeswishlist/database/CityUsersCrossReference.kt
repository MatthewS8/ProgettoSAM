package com.github.matthews8.placeswishlist.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(tableName = "city_user_cross_table",
    primaryKeys = ["cityId", "username"],
//    foreignKeys = [
//        ForeignKey(
//            entity = City::class,
//            parentColumns = arrayOf("cityId"),
//            childColumns = arrayOf("cityId"),
//            onDelete = ForeignKey.CASCADE,
//            onUpdate = ForeignKey.CASCADE),
//        ForeignKey(
//            entity = User::class,
//            parentColumns = arrayOf("username"),
//            childColumns = arrayOf("username"),
//            onDelete = ForeignKey.CASCADE,
//            onUpdate = ForeignKey.CASCADE)]
)
data class CityUsersCrossReference(
    val cityId: Long,
    val username: String,
)

