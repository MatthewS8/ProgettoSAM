package com.github.matthews8.placeswishlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase
import com.github.matthews8.placeswishlist.databinding.FragmentMainBinding
import com.github.matthews8.placeswishlist.mainfragment.MainFragmentViewModel
import com.github.matthews8.placeswishlist.mainfragment.MainFragmentViewModelFactory
import com.github.matthews8.placeswishlist.mainfragment.CityListAdapter

class MainFragment : Fragment() {

    private lateinit var viewModel: MainFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(
            inflater, R.layout.fragment_main, container, false)

        //viewModel
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MainFragmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MainFragmentViewModel::class.java)
        binding.mainFragmentViewModel = viewModel

        binding.addButton.setOnClickListener {
            it.findNavController().navigate(MainFragmentDirections.actionMainFragmentToMapsFragment())
            Toast.makeText(
                this.context,
                "Button clicked",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        binding.setLifecycleOwner(this)
        val adapter = CityListAdapter()
        binding.placesList.adapter = adapter
        viewModel.citiesList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}