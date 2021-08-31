package com.github.matthews8.placeswishlist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    City::class,
    User::class,
    Place::class,
    CityUsersCrossReference::class,
    PlaceUsersCrossReference::class,],
    version = 2,
    exportSchema = false)

abstract class FavPlacesDatabase: RoomDatabase() {
    abstract val favPlacesDatabaseDao: FavPlacesDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: FavPlacesDatabase? = null
        fun getInstance(context: Context): FavPlacesDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FavPlacesDatabase::class.java,
                        "fav_places_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

//todo test the database