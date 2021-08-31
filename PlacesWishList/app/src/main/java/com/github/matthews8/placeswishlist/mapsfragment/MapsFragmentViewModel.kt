package com.github.matthews8.placeswishlist.mapsfragment

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.database.FavPlacesDatabaseDao
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MapsFragmentViewModel(
    val database: FavPlacesDatabaseDao,
    application: Application): AndroidViewModel(application) {
    //qui va inserita la chiamata al database per prendere la lista dei luoghi
    //dichiarare la placeList che prende dal database la lista eventualmente nulla


    //todo vedere dove mettere sta roba
    val placeField: List<Place.Field> =
        listOf(Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ID,
        )

    var geo = Geocoder(application.baseContext)

    var lastMarker = MutableLiveData<Marker?>()
    var markerList: MutableList<Marker> = mutableListOf()
    var geoDone = false
    var citiesList = database.getCities()
    var gmap: GoogleMap? = null
    val addButtEnabled = Transformations.map(lastMarker) {
        Log.i("TAG ADDBUTT", "SONO NELLA TRASFORMATION")
        it?.title != null && it.title != "No title"
    }
    private val _navigateToMainFrament = MutableLiveData<Boolean?>()
    val navigateToMainFrament: LiveData<Boolean?>
        get() =_navigateToMainFrament



    fun initializeMap() {
       viewModelScope.launch(Dispatchers.Default) {
           citiesList.value?.forEach {city ->
               val mOpt = MarkerOptions()
                   .position(LatLng(city.lat, city.lng))
                   .title(city.name)
                   .snippet(city.country) //TODO
                   .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) //TODO <-- to change color
               gmap?.let {
                   val marker: Marker? = it.addMarker(mOpt)
                   marker?.let {markerList.add(marker)}
               }
           }
       }
    }


    fun onMarkerAdded(){
        Log.i("WheAREHERE", "this is the couritine $this@withContext")
        var addressList: List<Address>? = null
        //TODO check whether it's just a city or it's a place too

        Log.i("WheAREHERE", "INSIDE DISPATH IO this is the couritine $this@withContext")

        try {
            addressList = lastMarker.value?.position?.let {
                geo.getFromLocation(
                    it.latitude,
                    it.longitude,
                    1)
            }
        } catch(e: IOException) {
            //Check whether internet connection is ok
            e.printStackTrace()
        }
        Log.i("WheAREHERE", "INDA MAINthis is the couritine $this@withContext")
        if(addressList != null && addressList.isNotEmpty()){
            lastMarker.value?.let {
                it.title = addressList.first().adminArea ?: "No title"
                it.snippet = addressList.first().countryName ?: "No country"
                Log.i("ADDRESS", "address is ${addressList.first().adminArea}")
            }
        }

        geoDone = true

    }

    fun onDoneClicked(){
        if(geoDone){
            _navigateToMainFrament.value = true
        }

    }

    fun doneNavigating() {
        _navigateToMainFrament.value = null
    }

}

class MapsFrafmentViewModelFactory(
    private val dataSource: FavPlacesDatabaseDao,
    private val application: Application): ViewModelProvider.Factory{

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsFragmentViewModel::class.java)) {
            return MapsFragmentViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}