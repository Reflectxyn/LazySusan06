package com.example.lazy_susan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Define the adapter to handle the list of restaurants
class RestaurantAdapter(private val restaurants: List<Restaurant>) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    // ViewHolder to hold individual restaurant items
    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        val restaurantPhone: TextView = itemView.findViewById(R.id.restaurantPhone)
        val restaurantHours: TextView = itemView.findViewById(R.id.restaurantHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        // Inflate the layout for each item in the list
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        // Bind the restaurant data to the view
        val restaurant = restaurants[position]
        holder.restaurantName.text = restaurant.name
        holder.restaurantAddress.text = restaurant.address
        holder.restaurantPhone.text = restaurant.phoneNumber
        holder.restaurantHours.text = restaurant.hours
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }
}