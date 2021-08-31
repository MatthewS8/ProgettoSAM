package com.github.matthews8.placeswishlist.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.matthews8.placeswishlist.database.relations.CitiesWithPlacesAndUsers
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.github.matthews8.placeswishlist.database.relations.UserWithPlaces

@Dao
interface   FavPlacesDatabaseDao {

    @Insert
    suspend fun insertCity(city: City)

    /*@Transaction  TODO
    @Insert
    suspend fun insertPlaceAndCity(placeWithCity: CityWithPlaces)*/

//    @Transaction
//    @Insert
//    suspend fun insertUserAndPlaces(userWithPlaces: UserWithPlaces)

//    @Transaction
//    @Insert
//    suspend fun insertUserWithPlacesAndCities(citiesWithPlacesAndUsers: CitiesWithPlacesAndUsers)

    @Update
    suspend fun updateCity(city: City)

    @Update
    suspend fun updatePlace(place: Place)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM city_table WHERE cityId = :key")
    suspend fun getCity(key: Long): City?

    @Query("SELECT * FROM city_table")
    fun getCities(): LiveData<List<City>>

    @Query("SELECT * FROM place_table WHERE placeId = :key")
    suspend fun getPlace(key: Long): Place?

    @Query("SELECT * FROM user_table WHERE userId = :key")
    suspend fun getUser(key: Long): User?

    @Transaction
    @Query("SELECT * FROM city_table")
    suspend fun getCitiesWithPlaces(): List<CityWithPlaces>

    @Transaction
    @Query("SELECT * FROM city_table WHERE cityId = :key")
    fun getPlacesByCity(key: Long): LiveData<CityWithPlaces>

/* TODO
    Query DELETE
    Query Ordinamento e filtro
 */

}