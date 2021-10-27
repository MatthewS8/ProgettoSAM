package com.github.matthews8.placeswishlist.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.github.matthews8.placeswishlist.database.relations.CityWithPlacesAndUsers
import com.github.matthews8.placeswishlist.database.relations.CityWithUsers
import com.github.matthews8.placeswishlist.database.relations.PlaceWithUsers


@Dao
interface   FavPlacesDatabaseDao {

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: City) : Long

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlace(place: Place) : Long

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Transaction
    @Insert
    suspend fun insertCityUser(cityUsersCrossReference: CityUsersCrossReference)

    @Transaction
    @Insert
    suspend fun insertPlaceUser(placeUsersCrossReference: PlaceUsersCrossReference)

    @Update
    suspend fun updateCity(city: City)

    @Update
    suspend fun updatePlace(place: Place)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM city_table WHERE cityId = :key")
    suspend fun getCity(key: Long): City?

    @Query("SELECT cityId FROM city_table WHERE name = :cityName AND country = :country")
    suspend fun getCityId(cityName: String, country: String): Long?

    @Query("SELECT * FROM city_table")
    fun getCities(): LiveData<List<City>>

    @Query("SELECT * FROM place_table WHERE placeId = :key")
    suspend fun getPlace(key: Long): Place?

    @Query("SELECT * FROM user_table WHERE username = :username")
    suspend fun getUser(username: String): User?

    @Transaction
    @Query("SELECT * FROM city_table WHERE cityId = :cityId")
    fun getPlacesByCity(cityId: Long): LiveData<CityWithPlaces?>

    @Transaction
    @Query("SELECT * FROM city_table")
    fun getCitiesWithPlaces(): LiveData<List<CityWithPlaces>>

    @Transaction
    @Query("SELECT * FROM city_table WHERE cityId IN (:cityIds)")
    fun getCitiesWithPlaces(cityIds: List<Long>): List<CityWithPlaces>

    @Transaction
    @Query("SELECT * FROM city_table WHERE cityId = :cityId")
    fun getCityWithPlacesAndUsers(cityId: Long): LiveData<CityWithPlacesAndUsers?>


    @Transaction
    @Query("SELECT * FROM city_table")
    fun getCitiesWithUsers(): LiveData<List<CityWithUsers>>

    @Query ("SELECT * FROM place_table")
    fun getPlacesWithUsers(): LiveData<List<PlaceWithUsers>>

    @Query("SELECT * FROM place_table WHERE cityId = :cityId")
    fun myPersonalQuery(cityId: Long): LiveData<List<Place>>

    @Query("DELETE FROM city_table WHERE cityId = :cityId ")
    suspend fun deleteCity(cityId: Long)

    @Query("DELETE FROM city_table WHERE cityId IN (:cityIds)")
    suspend fun deleteCities(cityIds: List<Long>)

    @Query("DELETE FROM place_table WHERE placeId = :placeId ")
    suspend fun deletePlace(placeId: Long)

    @Query("SELECT * FROM city_table ORDER BY name ASC")
    fun getCitiesByNameAsc(): LiveData<List<City>>

    @Query("SELECT * FROM city_table ORDER BY name DESC")
    fun getCitiesByNameDesc(): LiveData<List<City>>

    @Query("SELECT * FROM city_table ORDER BY cityId ASC")
    fun getCitiesById(): LiveData<List<City>>

    @Query("SELECT * FROM city_table ORDER BY cityId DESC")
    fun getCitiesByIdDesc(): LiveData<List<City>>
}