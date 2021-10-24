package com.github.matthews8.placeswishlist.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.github.matthews8.placeswishlist.database.relations.CityWithPlacesAndUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseDaoHelper(val dataInstance: FavPlacesDatabaseDao){

    suspend fun insertPlaceAndCityWithOwner(city: City, place: Place? = null, owner: User = User()){
        var cityId = dataInstance.getCityId(cityName = city.name, country = city.country)
        if(cityId == null) {
            cityId = dataInstance.insertCity(city)
            .also {
                dataInstance.insertUser(owner)
                dataInstance.insertCityUser(CityUsersCrossReference(it, owner.username))
            }
        }
        if(cityId == -1L){
            cityId = dataInstance.getCityId(cityName = city.name, country = city.country)
        }
        place?.let {
            place.cityId = cityId!!
            val placeId = dataInstance.insertPlace(place)
                .also {
                    dataInstance.insertPlaceUser(PlaceUsersCrossReference(it, owner.username))
                }
        }
    }

    suspend fun cityVisitedToggle(cityId: Long){
        val city: City? = dataInstance.getCity(cityId)

        city?.let {
            it.visited = !it.visited
            dataInstance.updateCity(city)
        }
    }

}
