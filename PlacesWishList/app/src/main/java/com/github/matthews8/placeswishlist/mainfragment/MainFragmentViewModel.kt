package com.github.matthews8.placeswishlist.mainfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.database.DatabaseDaoHelper
import com.github.matthews8.placeswishlist.database.FavPlacesDatabaseDao
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MainFragmentViewModel(
    val database: FavPlacesDatabaseDao,
    application: Application): AndroidViewModel(application) {

/*    var pisa: City = City(
        cityId = 0,
        lat = LatLng(	43.716667, 	10.400).latitude,
        lng = LatLng(	43.716667, 	10.400).longitude,
        name = "Pisa",
        country = "Italy"
    )
    var firenze: City = City(
        cityId = 1,
        lat =  LatLng(43.769562, 	11.255814).latitude,
        lng =  LatLng(43.769562, 	11.255814).longitude,
        name = "Firenze",
        country = "Italy"
    )
    var bologna: City = City(
        cityId = 2,
        lat =  LatLng(44.498955, 11.327591).latitude,
        lng = LatLng(44.498955, 11.327591).longitude,
        name = "Bologna",
        country = "Italy"
    )
    var foggia: City = City(
        cityId = 3,
        lat =  LatLng(41.461761, 15.545021).latitude,
        lng = LatLng(41.461761, 15.545021).longitude,
        name = "Foggia",
        country = "Italy"
    ) */
    var munich: City = City(
        cityId = 4,
        lat =  LatLng(48.137154, 11.576124).latitude,
        lng = LatLng(48.137154, 11.576124).longitude,
        name = "Munchen",
        country = "Germany"
    )

    /*val pL = listOf(pisa, firenze, bologna, foggia, munich)*/


    var citiesList = database.getCities()
//    init {
//        viewModelScope.launch {
//            DatabaseDaoHelper(database, application).insertPlaceAndCityWithOwner(munich)
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MFVM","OnCleared called")
    }
}

class MainFragmentViewModelFactory(
    private val dataSource: FavPlacesDatabaseDao,
    private val application: Application): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)){
            return MainFragmentViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

data class PippoTest(
    val str: String,
    val pippo: Long,
    val pluto: LatLng = LatLng(0.0,0.0)
)