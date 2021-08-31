package com.github.matthews8.placeswishlist.mainfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.matthews8.placeswishlist.database.City
import com.github.matthews8.placeswishlist.R

class PlaceAdapter: RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {
    var data = listOf<City>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: PlaceAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount() = data.size


    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        val placeName: TextView = itemView.findViewById(R.id.place_name_text)
        val placeCountry: TextView = itemView.findViewById(R.id.place_country_text)
        val image: ImageView  = itemView.findViewById(R.id.directions_icon_image)

        fun bind(item: City) {
            val res = itemView.context.resources
            placeName.text = item.name
            placeCountry.text = item.country
        }

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.list_item_place, parent, false)
                return ViewHolder(view)
            }
        }
    }
}
