package com.github.matthews8.placeswishlist.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.github.matthews8.placeswishlist.database.Place
import com.github.matthews8.placeswishlist.database.PlaceUsersCrossReference
import com.github.matthews8.placeswishlist.database.User

data class UserWithPlaces(
    @Embedded val user: User,
    @Relation(
        parentColumn = "username",
        entityColumn = "placeId",
        associateBy = Junction(PlaceUsersCrossReference::class)
    )
    val places: List<Place>

)
