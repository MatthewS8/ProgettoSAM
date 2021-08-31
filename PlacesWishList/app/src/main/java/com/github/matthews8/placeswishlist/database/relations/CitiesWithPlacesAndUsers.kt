package com.github.matthews8.placeswishlist.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.database.Place

data class CitiesWithPlacesAndUsers(
    @Embedded val city: City,
    @Relation(
        entity = Place::class,
        parentColumn = "cityId",
        entityColumn = "cityId"
    )
    val placesAndUsers: List<PlaceWithUsers>
)
