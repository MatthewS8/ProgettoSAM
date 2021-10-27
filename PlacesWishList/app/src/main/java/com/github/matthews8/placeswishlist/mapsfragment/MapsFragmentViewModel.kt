package com.github.matthews8.placeswishlist.mapsfragment

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.*
import com.github.matthews8.placeswishlist.BuildConfig
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.*
import com.github.matthews8.placeswishlist.database.relations.CityWithUsers
import com.github.matthews8.placeswishlist.database.relations.PlaceWithUsers
import com.github.matthews8.placeswishlist.utils.GeocoderResponse
import com.github.matthews8.placeswishlist.utils.RetrofitImpl
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.libraries.places.api.model.Place as gPlace


class MapsFragmentViewModel(
    val database: FavPlacesDatabaseDao,
    application: Application): AndroidViewModel(application) {

    val TAG = "MapsFragmentVM"

    val placeField: List<gPlace.Field> =
        listOf(
            gPlace.Field.NAME,
            gPlace.Field.LAT_LNG,
            gPlace.Field.ADDRESS,
            gPlace.Field.ID,
            gPlace.Field.TYPES,
            gPlace.Field.ADDRESS_COMPONENTS)

    private val daoHelper = DatabaseDaoHelper(database)
    lateinit var lastMarker: Marker
    var markerList: MutableList<Marker> = mutableListOf() //eliminabile
    var cityWithUsers: LiveData<List<CityWithUsers>> = database.getCitiesWithUsers()
    var gmap: GoogleMap? = null

    var city = MutableLiveData<City?>()
    var place: Place? = null

    var choice = MutableLiveData<Int?>()

    val addButtEnabled = Transformations.map(city) {
        it?.let {
            it.country.isNotBlank() && it.name.isNotBlank()
        }?: false
    }

    private val _navigateToMainFragment = MutableLiveData<Boolean?>()
    val navigateToMainFragment: LiveData<Boolean?>
        get() =_navigateToMainFragment


    private val _navigateToDialog = MutableLiveData<Boolean?>()
    val navigateToDialog: LiveData<Boolean?>
        get() =_navigateToDialog

    fun initializeMap() {
       viewModelScope.launch(Dispatchers.Default) {
           cityWithUsers.value?.forEach {
               Log.i(TAG, "initializeMap: ${it.city.name}, ${it.users.first().username}")
           }
           

           cityWithUsers.value?.forEach {cwu ->
               val mOpt = MarkerOptions()
                   .position(LatLng(cwu.city.lat, cwu.city.lng))
                   .title(cwu.city.name)
                   .snippet("${cwu.city.country}, ${cwu.users.first().username}")
                   .icon(BitmapDescriptorFactory.defaultMarker(
                       cwu.users.first().color_picked))
               withContext(Dispatchers.Main) {
                   gmap?.let {
                       val marker: Marker? = it.addMarker(mOpt)
                       marker?.let {markerList.add(marker)}
                   }
               }
           }

           //Serve a riposizionare il marker nel caso di ScreenRotation
           withContext(Dispatchers.Main) {
               if(this@MapsFragmentViewModel::lastMarker.isInitialized) {
                   val mark = gmap?.addMarker(MarkerOptions()
                       .title(lastMarker.title)
                       .position(lastMarker.position))
                   mark?.let{
                       lastMarker = it
                   }
               }
           }
       }
    }

    fun addMarker(latLng: LatLng, placeG: gPlace? = null){
        city.value = null
        place = null
        if(this::lastMarker.isInitialized) {
            //Rimuovo il marker precedente
            lastMarker.remove()
        }
        val mark = gmap!!.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("")
        )
        mark?.let {
           lastMarker = it
        }

        if(placeG == null) geocodingApiCall(latLng)
        else{
            onPlaceSelected(placeG, latLng)
        }
    }

    private fun geocodingApiCall(latLng: LatLng) {
        Log.i(TAG, "geocodingApiCall: Geocodind api has been called")

        viewModelScope.launch(Dispatchers.IO) {
            (RetrofitImpl.geocodeApi
                .getGeocoding(
                key = BuildConfig.GEOCODING_API_KEY,
                latLng = "${latLng.latitude},${latLng.longitude}",
            )?.enqueue(object: Callback<GeocoderResponse?>{

                override fun onResponse(
                    call: Call<GeocoderResponse?>,
                    response: Response<GeocoderResponse?>) {
                    Log.i("geocodingApiCall", "response is ${response.body()}")
                    onSuccessCall(response.body(), latLng)
                }

                override fun onFailure(call: Call<GeocoderResponse?>, t: Throwable) {
                    Log.e("geocodingApiCall", "onFailure: $t", )
                }
            }))
        }
    }

    private fun onSuccessCall(geocoderResponse: GeocoderResponse?, latLng: LatLng){

        Log.i(TAG, "onSuccessCall: parsing the response that is ${geocoderResponse.toString()}")

        city.value = null
        place = null

            geocoderResponse?.let {geoResp ->
                Log.i(TAG, "Ci sono ${geoResp.results.size} results")

                geoResp.results.forEach {results ->
                    if(city.value == null
                        || city.value?.name!!.isBlank()
                        || city.value?.country!!.isBlank()
                        || place == null
                    ) {
                        results.address_components.forEach {
                            if(it.types.contains("premise")
                                || it.types.contains("point_of_interest")
                            ) {

                                Log.i(TAG, "premise or point_ofInterest: ${it.long_name}")

                                if(place == null)
                                    place = Place(
                                        place_id = results.place_id,
                                        name = it.long_name,
                                        address = results.formatted_address,
                                        cityId = -1L
                                    )

                            }
                            if(it.types.contains("country")) {

                                Log.i(TAG, "country; ${it.long_name}")

                                if(city.value == null) {
                                    city.value = City(
                                        lat = latLng.latitude,
                                        lng = latLng.longitude,
                                        country = it.long_name,
                                        name = ""
                                    )
                                    viewModelScope.launch(Dispatchers.Main) {
                                        lastMarker.snippet = it.long_name
                                    }
                                } else {
                                    if(city.value!!.country.isBlank()) {
                                        val name = city.value?.name
                                        city.value = City(
                                            lat = latLng.latitude,
                                            lng = latLng.longitude,
                                            country = it.long_name,
                                            name = name ?: ""
                                        )
                                    }
                                    viewModelScope.launch(Dispatchers.Main) {
                                        lastMarker.snippet = it.long_name
                                    }
                                }
                            }

                            if(it.types.contains("locality")) {
                                Log.i(TAG, "locality; ${it.long_name}")
                                if(city.value == null) {
                                    city.value = City(
                                        lat = latLng.latitude,
                                        lng = latLng.longitude,
                                        country = "",
                                        name = it.long_name
                                    )
                                    viewModelScope.launch(Dispatchers.Main) {
                                        lastMarker.title = it.long_name
                                    }
                                } else {
                                    if(city.value!!.name.isBlank()) {
                                        val country = city.value?.country
                                        city.value = City(
                                            lat = latLng.latitude,
                                            lng = latLng.longitude,
                                            country = country ?: "",
                                            name = it.long_name
                                        )

                                    }

                                }
                            }
                        }
                    }
                }

            }

        lastMarker.showInfoWindow()
    }

    private fun onPlaceSelected(placeG: gPlace, latLng: LatLng){

            val pType = placeG.types?.let {
                placeTypeSelector(it)
            }

            if(pType == gPlace.Type.POINT_OF_INTEREST
                || pType == gPlace.Type.OTHER
            ) {
                place = Place(
                    place_id = placeG.id,
                    name = placeG.name ?: "",
                    address = placeG.address ?: "",
                    type = pType,
                    cityId = -1L
                )
                viewModelScope.launch (Dispatchers.Main){
                    lastMarker.title = placeG.name
                }
            }
            placeG.addressComponents?.asList()?.forEach {
                if(it.types.contains("country")) {
                    if(city.value == null) {
                        city.value = City(
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            country = it.name,
                            name = ""
                        )
                    } else if(city.value!!.country.isBlank()) {
                        val name = city.value!!.name
                        city.value = City(
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            country = it.name,
                            name = name
                        )
                    }
                    viewModelScope.launch (Dispatchers.Main){
                        lastMarker.snippet = it.name
                    }
                } else if(it.types.contains("locality")) {
                    if(city.value == null) {
                        city.value = City(
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            country = "",
                            name = it.name
                        )
                    } else if(city.value!!.name.isBlank()) {
                        val country = city.value!!.country
                        city.value = City(
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            country = country,
                            name = it.name
                        )
                    }
                    viewModelScope.launch (Dispatchers.Main){
                        if(lastMarker.title != null
                            && lastMarker.title!!.isNotBlank()
                        ) lastMarker.title = it.name
                    }
                }
            }

        Log.i(TAG, "onPlaceSelected: city value is ${city.value}")

        lastMarker.showInfoWindow()
    }

    fun placeTypeSelector(types: List<gPlace.Type>): gPlace.Type?{
        val poi = listOf(
            gPlace.Type.POINT_OF_INTEREST,
            gPlace.Type.TOURIST_ATTRACTION,
            gPlace.Type.MUSEUM,
            gPlace.Type.ESTABLISHMENT)
        if(types.any{it in poi}) return gPlace.Type.POINT_OF_INTEREST
        if(types.contains(gPlace.Type.LOCALITY)) return gPlace.Type.LOCALITY
        if(types.contains(gPlace.Type.COUNTRY)) return null
        return gPlace.Type.OTHER
    }

    fun onAddClicked(){
        Log.i("TAG", "onAddClicked: place name is ${place?.name}")

        if(place != null && place!!.name.isNotBlank()){
            _navigateToDialog.value = true
        } else{
            Log.i("TAG", "onAddClicked: in the else calling database i hope")

            GlobalScope.launch(Dispatchers.IO) {
                daoHelper.insertPlaceAndCityWithOwner(city.value!!)
            }

            _navigateToMainFragment.value = true

            Log.i("TAG", "onAddClicked: navigate to main is : ${_navigateToMainFragment.value}")
        }
    }


    fun doneNavigating() {
        _navigateToMainFragment.value = null
    }

    fun doneNavigatingToDialog(){
        _navigateToDialog.value = null
    }

    fun choiceDone() {
        Log.i(TAG, "choiceDone: choice value is :$choice")

        when(choice.value!!){
            0 -> {
                Log.i("TAG", "choiceDone: choice 0")
                GlobalScope.launch() {
                    daoHelper.insertPlaceAndCityWithOwner(city = city.value!!, place = place)
                }
            }
            1 -> {
                GlobalScope.launch() {
                    daoHelper.insertPlaceAndCityWithOwner(city = city.value!!)
                }
            }
            else -> {
                Toast.makeText(getApplication<Application>().applicationContext,
                    R.string.err_message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        choice.value = null
    }
}

class MapsFrafmentViewModelFactory(
    private val dataSource: FavPlacesDatabaseDao,
    private val application: Application): ViewModelProvider.Factory{


    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsFragmentViewModel::class.java)) {
            if(INSTANCE == null)
                INSTANCE = MapsFragmentViewModel(dataSource, application)
                return INSTANCE as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object{
        private var INSTANCE: MapsFragmentViewModel? = null
        fun setInstanceToNull(){
            INSTANCE = null
        }

    }

}