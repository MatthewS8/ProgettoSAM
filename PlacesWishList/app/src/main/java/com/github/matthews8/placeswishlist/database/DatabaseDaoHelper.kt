package com.github.matthews8.placeswishlist.database

import android.content.Context

class DatabaseDaoHelper(val dataInstance: FavPlacesDatabaseDao, context: Context){

    suspend fun insertPlaceAndCityWithOwner(city: City, place: Place? = null, owner: User = User()){
        val cityId = dataInstance.insertCity(city)
            .also {
                dataInstance.insertUser(owner)
                dataInstance.insertCityUser(CityUsersCrossReference(it, owner.username))
            }
        place?.let {
            place.cityId = cityId
            val placeId = dataInstance.insertPlace(place)
                .also {
                    dataInstance.insertPlaceUser(PlaceUsersCrossReference(it, owner.username))
                }
        }
    }
}
