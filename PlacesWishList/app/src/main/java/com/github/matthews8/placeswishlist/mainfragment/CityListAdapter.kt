package com.github.matthews8.placeswishlist.mainfragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.databinding.ListItemCityBinding

class CityListAdapter(val cityClickListener: CityListener): ListAdapter<City, CityListAdapter.ViewHolder>(CityListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), cityClickListener)
    }



    class ViewHolder private constructor(val binding: ListItemCityBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: City, clickListeners: CityListener) {
            if(item.visited) binding.visited.setColorFilter(Color.BLACK)
            else binding.visited.clearColorFilter()
            binding.city = item
            binding.cityClickListener = clickListeners
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

class CityListener(val cityClickListener: (cityId: Long) -> Unit, val iconClickListener: (cityId: Long) -> Unit){
    fun onClick(city: City) = cityClickListener(city.cityId)
    fun onIconClick(city: City) = iconClickListener(city.cityId)
}