package com.github.matthews8.placeswishlist.mainfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.databinding.ListItemCityBinding

class CityListAdapter: ListAdapter<City, CityListAdapter.ViewHolder>(CityListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }



    class ViewHolder private constructor(val binding: ListItemCityBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: City) {
            binding.city = item
            binding.executePendingBindings()
        }

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemCityBinding.inflate(layoutInflater,parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class CityListDiffCallback: DiffUtil.ItemCallback<City>(){
    override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem.cityId == newItem.cityId
    }

    override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
        return oldItem == newItem
    }

}
