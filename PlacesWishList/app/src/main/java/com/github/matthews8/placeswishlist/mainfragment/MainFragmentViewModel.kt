package com.github.matthews8.placeswishlist.mainfragment

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
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

    var citiesList = database.getCities()

    private val _navigateToPlaceList = MutableLiveData<Long?>()
    val navigateToPlaceList: LiveData<Long?>
        get() = _navigateToPlaceList

    fun onCityClicked(cityId: Long){
        _navigateToPlaceList.value = cityId
    }

    fun onIconClicked(cityId: Long){
        viewModelScope.launch {
            DatabaseDaoHelper(database, getApplication<Application>().applicationContext)
                .cityVisitedToggle(cityId)
        }
    }

    fun onPlaceListNavigated(){
        _navigateToPlaceList.value = null
    }

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