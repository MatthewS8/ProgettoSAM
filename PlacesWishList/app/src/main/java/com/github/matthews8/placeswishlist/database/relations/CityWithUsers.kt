package com.github.matthews8.placeswishlist.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.database.CityUsersCrossReference
import com.github.matthews8.placeswishlist.database.User

data class CityWithUsers(
    @Embedded val city: City,
    @Relation(
        parentColumn = "cityId",
        entityColumn = "userId",
        associateBy = Junction(CityUsersCrossReference::class)
    )
    val users: List<User>
)