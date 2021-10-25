package com.github.matthews8.placeswishlist.mainfragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.R
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.databinding.ListItemCityBinding

class CityListAdapter(val cityClickListener: CityListener): ListAdapter<City, CityListAdapter.ViewHolder>(CityListDiffCallback()) {

    var tracker: SelectionTracker<Long>? = null
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let {
            val isSelected = tracker?.isSelected(it.cityId) ?: false
            holder.bind(it, cityClickListener, isSelected)

        }
    }

    override fun getItemId(position: Int): Long = getItem(position).cityId

    class ViewHolder private constructor(val binding: ListItemCityBinding): RecyclerView.ViewHolder(binding.root){

        val TAG = "VIEWHOLDER"
        fun bind(item: City, clickListeners: CityListener, isSelected: Boolean) {
            if(isSelected) {
                binding.visited.visibility = View.GONE
            } else {
                binding.visited.visibility = View.VISIBLE
                if(item.visited) binding.visited.setColorFilter(Color.BLACK)
                else binding.visited.clearColorFilter()
            }
            itemView.isSelected = isSelected
            binding.city = item
            binding.cityClickListener = clickListeners
            binding.executePendingBindings()
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition() = adapterPosition
                override fun getSelectionKey() = itemId
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

class CityDetailsLookup(
    private val rv: RecyclerView): ItemDetailsLookup<Long>(){
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = rv.findChildViewUnder(e.x, e.y)
        if(view != null){
            return (rv.getChildViewHolder(view) as CityListAdapter.ViewHolder)
                .getItemDetails()
        }
        return null
    }
    }