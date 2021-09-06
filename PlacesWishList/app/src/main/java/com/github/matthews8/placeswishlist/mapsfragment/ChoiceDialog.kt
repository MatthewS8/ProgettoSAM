package com.github.matthews8.placeswishlist.mapsfragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.FavPlacesDatabase

class ChoiceDialog: DialogFragment() {
    private lateinit var viewModel: MapsFragmentViewModel
    val args: ChoiceDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.choice_dialog, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = FavPlacesDatabase.getInstance(application).favPlacesDatabaseDao
        val viewModelFactory = MapsFrafmentViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MapsFragmentViewModel::class.java)

        val option1: TextView = view.findViewById(R.id.firstOption)
        val option2: TextView = view.findViewById(R.id.secondOption)
        option1.apply {
            text = args.option1
            setOnClickListener {
                viewModel.choice.value = 1
                it.findNavController().popBackStack()
            }
        }
        option2.apply {
            text = args.option2
            setOnClickListener {
                viewModel.choice.value = 2
                it.findNavController().popBackStack()
            }
        }

        return view
    }
}