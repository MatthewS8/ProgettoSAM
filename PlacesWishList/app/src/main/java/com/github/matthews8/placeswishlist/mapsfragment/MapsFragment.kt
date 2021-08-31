package com.github.matthews8.placeswishlist.mapsfragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.databinding.FragmentMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.launch

class MapsFragment : Fragment() {
    private lateinit var viewModel: MapsFragmentViewModel
    private lateinit var binding: FragmentMapsBinding

    private val callback = OnMapReadyCallback {googleMap ->

        viewModel.gmap = googleMap
        viewModel.initializeMap()

        googleMap.setOnMapClickListener { latLng ->
            viewModel.geoDone = false
            //TODO getPlace

            //Rimuovo il marker precedente
            viewModel.lastMarker.value?.remove() ?:
            viewModel.lastMarker.observe(this, Observer{
                if(it?.title != "" || it.title != "No Title")
                    it?.showInfoWindow()
            })

            val mark = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("")
            )
            viewModel.lastMarker.value = mark

            viewModel.onMarkerAdded() // TODO Check this out



        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MapsFrafmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MapsFragmentViewModel::class.java)

        Places.initialize(requireContext(), getString(R.string.api_key))
        val placesClient = Places.createClient(this.requireContext())

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps,container, false)
        binding.addButton.setOnClickListener {
            findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToMainFragment())
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val token = AutocompleteSessionToken.newInstance()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val acFrag: AutocompleteSupportFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
//        acFrag.setTypeFilter(TypeFilter.ESTABLISHMENT)
        acFrag.setPlaceFields(viewModel.placeField)
        mapFragment?.getMapAsync(callback)
        acFrag.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                //todo- ------------------------------
                Toast.makeText(context, "Place: ${place.name}, ${place.address} ${place.latLng}",Toast.LENGTH_LONG).show()
                viewModel.gmap?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,10f))
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                when(status.statusCode){
                    PlacesStatusCodes.NOT_FOUND -> {}
                    PlacesStatusCodes.INVALID_REQUEST -> {}
                    else -> {}
                }
                Toast.makeText(context, "an error occured. check the log", Toast.LENGTH_LONG).show()
                Log.i("MapsFragment", "An error occurred: $status")
            }
        })
    }
}

//TODO TO ADD A MARKER ON TAP
/**
 * map.setOnMapClickListener {
 *   allPoints.add(it)
 *   map.clear()
 *   map.addMarker(MarkerOptions().position(it))
 * }
 */