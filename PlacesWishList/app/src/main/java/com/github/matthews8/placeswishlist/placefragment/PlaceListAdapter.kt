package com.github.matthews8.placeswishlist.placefragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.database.Place
import com.github.matthews8.placeswishlist.databinding.ListItemPlaceBinding

class PlaceListAdapter: ListAdapter<Place, PlaceListAdapter.ViewHolder>(PlaceListDiffCallback()) {
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val placeItem = getItem(position)
        holder.bind(placeItem)
    }


    class ViewHolder private constructor(val binding: ListItemPlaceBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Place) {
            binding.place = item
            binding.executePendingBindings()
        }

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemPlaceBinding.inflate(layoutInflater,parent, false)
                return ViewHolder(binding)
            }
        }
    }

}

class PlaceListDiffCallback: DiffUtil.ItemCallback<Place>(){
    override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
        return oldItem.placeId == newItem.placeId
    }

    override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
        return oldItem == newItem
    }

}