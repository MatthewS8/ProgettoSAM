package com.github.matthews8.placeswishlist.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.database.Place

data class CityWithPlaces(
    @Embedded val city: City,
    @Relation(
        parentColumn = "cityId",
        entityColumn = "cityId"
    )
    val places: List<Place>
)
