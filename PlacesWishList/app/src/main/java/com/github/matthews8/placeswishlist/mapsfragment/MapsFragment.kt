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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.matthews8.placeswishlist.BuildConfig
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.database.Place
import com.github.matthews8.placeswishlist.databinding.FragmentMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place as gPlace
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class MapsFragment : Fragment() {
    private lateinit var viewModel: MapsFragmentViewModel
    private lateinit var binding: FragmentMapsBinding
    val TAG = "Fede_NArgi"
    val args: MapsFragmentArgs by navArgs()
    private val callback = OnMapReadyCallback {googleMap ->

        viewModel.gmap = googleMap



        Log.i("OnMapsCallback", "OnMapsCallback: Callback triggered ")
        googleMap.setOnMapClickListener { latLng ->
            viewModel.addMarker(latLng)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        if(args.fromMainFragment) MapsFrafmentViewModelFactory.setInstanceToNull() //seems to work now but check if there are problems here
        val viewModelFactory = MapsFrafmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MapsFragmentViewModel::class.java)

        Places.initialize(requireContext(), BuildConfig.GOOGLE_MAPS_API_KEY)
        val placesClient = Places.createClient(this.requireContext())

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps,container, false)
        binding.mapsFragmentViewModel = viewModel

        viewModel.citiesList.observe(viewLifecycleOwner, Observer {
            if(viewModel.gmap != null && it != null) {
                viewModel.initializeMap()
            }
            Log.i(TAG, "onCreateView: citiesList trigger")
        })
        viewModel.navigateToMainFragment.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToMainFragment())
                viewModel.doneNavigating()
            }
            Log.i(TAG, "onCreateView: navigate to MAIN fragment ")
        })

        viewModel.navigateToDialog.observe(viewLifecycleOwner, Observer {
            it?.let {findNavController()
                .navigate(MapsFragmentDirections
                    .actionMapsFragmentToChoiceDialog(
                        viewModel.place!!.address,
                        viewModel.city.value!!.name
                    + ", ${viewModel.city.value!!.country}"))
                viewModel.doneNavigatingToDialog()
            }
            Log.i(TAG, "onCreateView:navigate to dialog ${viewModel.navigateToDialog.value}")
        })

        viewModel.choice.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.choiceDone()
                findNavController().navigate(MapsFragmentDirections
                    .actionMapsFragmentToMainFragment())
            }
            Log.i(TAG, "onCreateView: choice trigger and its value is ${viewModel.choice.value}")

        })


        binding.setLifecycleOwner(this)
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
            override fun onPlaceSelected(place: gPlace) {
                //todo- ------------------------------
                Log.i("PlaceSelected",
                    "place: ${place.name}, ${place.address} ${place.latLng} ${place.addressComponents} ${place.types}")
                viewModel.addMarker(place.latLng!!, place)

                var zoom = 10f
                place.types?.let {
                    if (it.contains(gPlace.Type.TOURIST_ATTRACTION)
                        || it.contains(gPlace.Type.POINT_OF_INTEREST)
                        || it.contains(gPlace.Type.ESTABLISHMENT) )
                        zoom = 15f
                }
                viewModel.gmap?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, zoom))

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                when(status.statusCode){
                    PlacesStatusCodes.NOT_FOUND -> {}
                    PlacesStatusCodes.INVALID_REQUEST -> {}
                    PlacesStatusCodes.CANCELED -> {}
                    else -> {}
                }
                Toast.makeText(context, "an error occured. check the log $status", Toast.LENGTH_LONG).show()
                Log.i(TAG, "oNPLACESELECTED: An error occurred: $status")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: viewModel ${viewModel.choice.value}")
        //To avoid the view model reset due to screen rotation
//        if(viewModel.choice.value != null) {
//            MapsFrafmentViewModelFactory.setInstanceToNull()
//        }
    }
}