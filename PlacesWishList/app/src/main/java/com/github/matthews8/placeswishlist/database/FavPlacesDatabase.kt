package com.github.matthews8.placeswishlist.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomOpenHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(entities = [
    City::class,
    User::class,
    Place::class,
    CityUsersCrossReference::class,
    PlaceUsersCrossReference::class,
],
    version = 1,
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
                        .addCallback(CALLBACK)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                GlobalScope.launch(Dispatchers.IO) {
                    INSTANCE?.favPlacesDatabaseDao?.insertUser(User())
                }
            }
        }
    }
}

//todo test the database