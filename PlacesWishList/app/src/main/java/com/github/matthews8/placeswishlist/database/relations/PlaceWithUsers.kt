package com.github.matthews8.placeswishlist.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.github.matthews8.placeswishlist.database.Place
import com.github.matthews8.placeswishlist.database.PlaceUsersCrossReference
import com.github.matthews8.placeswishlist.database.User

data class PlaceWithUsers(
    @Embedded val place: Place,
    @Relation(
        parentColumn = "placeId",
        entityColumn = "userId",
        associateBy = Junction(PlaceUsersCrossReference::class)
    )
    val users: List<User>

)
