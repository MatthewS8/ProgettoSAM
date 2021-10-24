package com.github.matthews8.placeswishlist.placefragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.databinding.FragmentPlaceBinding

class PlaceFragment : Fragment() {
    private lateinit var viewModel: PlaceFragmentViewModel
    val args: PlaceFragmentArgs by navArgs()

    val TAG: String = "Munichers"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentPlaceBinding>(
            inflater, R.layout.fragment_place, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = PlaceFragmentViewModelFactory(dataSource, application, args.cityId)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(PlaceFragmentViewModel::class.java)
        binding.placeFragmentViewModel = viewModel

        binding.setLifecycleOwner (this)
        val adapter = PlaceListAdapter() //todo fare qui

        binding.placesList.adapter = adapter
        viewModel.placesbyCity.observe(viewLifecycleOwner, Observer { list ->
            adapter.submitList(list?.places)
        })

        //------------------
        requireActivity().actionBar?.title = viewModel.placesbyCity.value?.city!!.name
        //------------------

        return binding.root
    }
}