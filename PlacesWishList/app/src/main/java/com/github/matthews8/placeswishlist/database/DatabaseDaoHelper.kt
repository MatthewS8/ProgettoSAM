package com.github.matthews8.placeswishlist.database

import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces

class DatabaseDaoHelper(val dataInstance: FavPlacesDatabaseDao){

    val TAG = "dbHelper"
    suspend fun insertPlaceAndCityWithOwner(city: City, place: Place? = null, owner: User = User()){
        var cityId = dataInstance.getCityId(cityName = city.name, country = city.country)
        var username = dataInstance.getUser(owner.username)
        if(username == null) {
            dataInstance.insertUser(owner)
        }
        if(cityId == null) {
            cityId = dataInstance.insertCity(city)
            .also {
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

    suspend fun saveUserPlaces(listReceived: Array<CityWithPlaces>?, user: User) {
        listReceived?.let { list ->
            for(cityWithPlaces in list){
                if(cityWithPlaces.places.isEmpty()) {
                    insertPlaceAndCityWithOwner(cityWithPlaces.city, null, user)
                } else {
                    for(places in cityWithPlaces.places)
                        insertPlaceAndCityWithOwner(cityWithPlaces.city, places, user)
                }
            }
        }

    }

}
