package com.github.matthews8.placeswishlist.mainfragment

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.database.*
import com.github.matthews8.placeswishlist.database.relations.CityWithPlaces
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var previousOrderBy = 0
    var orderBy = MutableLiveData<Int?>()

    //TODO BUG strano comportamento se ordino la lista mi fa solo un ordinamento parziale
    //  credo che il problema sia nella sequenza eventi tra ricezione della lista e registrazione del observer
    fun orderByName(){
        orderBy.value = when(orderBy.value) {
            0 -> 1
            1 -> 0
            else -> {
                //null
                0
            }
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
            DatabaseDaoHelper(database)
                .cityVisitedToggle(cityId)
        }
    }

    var selectionToSend: ByteArray? = null
    var selectedList: List<Long>? = null


    fun deleteSelectedCities(toRemove: List<Long>) {
        Log.i(TAG, "deleteSelectedCities: received this list")
        for( i in toRemove){
            Log.i(TAG, "LIST: $i")
        }
        viewModelScope.launch {
            deleteFromDB(toRemove)
        }
    }

    private suspend fun deleteFromDB(cities: List<Long>){
        database.deleteCities(cities)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG,"OnCleared called")
    }

    suspend fun saveUserPlaces(listReceived: Array<CityWithPlaces>?, user: User) {
        Log.i(TAG, "saveUserPlaces: list ${listReceived.toString()}, user : ${user.toString()}")
        DatabaseDaoHelper(database).saveUserPlaces(listReceived, user)
    }
}

class MainFragmentViewModelFactory(
    private val dataSource: FavPlacesDatabaseDao,
    private val application: Application): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)){
            if(INSTANCE == null)
                INSTANCE = MainFragmentViewModel(dataSource, application)
            return INSTANCE as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object{
        private var INSTANCE: MainFragmentViewModel? = null
        fun setInstanceToNull(){
            INSTANCE = null
        }

    }

}