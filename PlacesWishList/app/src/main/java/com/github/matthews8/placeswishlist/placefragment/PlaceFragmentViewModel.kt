package com.github.matthews8.placeswishlist.placefragment

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.database.FavPlacesDatabaseDao
import com.github.matthews8.placeswishlist.database.Place
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.github.matthews8.placeswishlist.database.relations.CityWithPlacesAndUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException


/* TODO
 *    il probema è che la places list non si aggiorna automaticamento e
 *    in realtaà non ce ne sarebbe nemmeno bisogno in quanto se sono in questa schermata
 *    non ho la possibilita di aggiungere nuovi posti (o si? -- nel caso bisogna fare submitlist
 *    al ritorno dalla funzione che aggiunge il place)
 */
class PlaceFragmentViewModel(
    val database: FavPlacesDatabaseDao,
    application: Application, val cityId: Long): AndroidViewModel(application){

    val TAG: String = "Munichers"
//    var cityWithPlaces: CityWithPlaces? = null

//    val placesbyCity = database.myPersonalQuery(cityId)
    val placesbyCity = database.getPlacesByCity(cityId)
//    val cityWithPlacesAndUsers: LiveData<CityWithPlacesAndUsers?> =
//        database.getCityWithPlacesAndUsers(cityId)
//
//    init{
//        viewModelScope.launch(Dispatchers.IO) {
//            initPlacesList()
//        }
//    }
//    private suspend fun initPlacesList() {
//        cityWithPlaces = database.getPlacesByCity(cityId)
//    }

}

class PlaceFragmentViewModelFactory(
    private val dataSource: FavPlacesDatabaseDao,
    private val application: Application,
    private val cityId: Long
): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceFragmentViewModel::class.java)){
            return PlaceFragmentViewModel(dataSource, application, cityId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}