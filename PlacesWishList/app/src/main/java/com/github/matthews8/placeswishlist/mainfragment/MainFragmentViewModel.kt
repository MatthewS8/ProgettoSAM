package com.github.matthews8.placeswishlist.mainfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.database.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MainFragmentViewModel(
    val database: FavPlacesDatabaseDao,
    application: Application): AndroidViewModel(application) {

    val TAG = this.javaClass.name

    var citiesList = database.getCities()

    /** order by
     *  name asc 0 desc 1
     *  added asc 2 desc 3
     */
    var orderBy = MutableLiveData<Int?>()

    //TODO BUG strano comportamento se ordino la lista mi fa solo un ordinamento parziale
    //  credo che il problema sia nella sequenza eventi tra ricezione della lista e registrazione del observer
    fun orderByName(){
        orderBy.value = when(orderBy.value) {
            0 -> 1
            else -> 0
        }
        orderList()
    }

    fun orderById(){
        orderBy.value = when(orderBy.value) {
            2 -> 3
            else -> 2
        }
        orderList()
    }

    private fun orderList(){
        citiesList = when(orderBy.value){
            0 -> database.getCitiesByNameAsc()
            1 -> database.getCitiesByNameDesc()
            2 -> database.getCitiesByIdDesc()
            3 -> database.getCitiesById()
            else -> database.getCities()
        }
    }

//    init{
//        //aggiungo un utente al database con una citta e un place
//        val munich = City(lat = 48.1373932, lng = 11.5754485, name = "München",
//        country = "Germany")
//        val marienplatz = Place(place_id = "ChIJLWM3jSROqEcRswsOX7NRrd4",
//        name = "Marienplatz", address = "Marienplatz, 80331 München, Germany",
//            type = com.google.android.libraries.places.api.model.Place.Type.POINT_OF_INTEREST, cityId = 0L)
//        val gerardo = User(username = "Gerardo", color_picked = BitmapDescriptorFactory.HUE_VIOLET)
//
//        val dao = DatabaseDaoHelper(database)
//        viewModelScope.launch (Dispatchers.IO) {
//            dao.insertPlaceAndCityWithOwner(city = munich, place = marienplatz, owner = gerardo)
//        }
//    }

    private val _navigateToPlaceList = MutableLiveData<Long?>()
    val navigateToPlaceList: LiveData<Long?>
        get() = _navigateToPlaceList

    fun onPlaceListNavigated(){
        _navigateToPlaceList.value = null
    }

    fun onCityClicked(cityId: Long){
        _navigateToPlaceList.value = cityId
    }

    fun onIconClicked(cityId: Long){
        viewModelScope.launch {
            DatabaseDaoHelper(database) //, getApplication<Application>().applicationContext)
                .cityVisitedToggle(cityId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG,"OnCleared called")
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