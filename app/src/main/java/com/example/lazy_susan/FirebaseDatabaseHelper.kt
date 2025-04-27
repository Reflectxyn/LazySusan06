package com.example.lazy_susan

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log

object FirebaseDatabaseHelper {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    //save a restaurant to Firebase realtime

    fun saveRestaurantToFirebase(userId: String, restaurant: Restaurant) {
        val restaurantId = database.child("users").child(userId).child("userRestaurants").push().key

        restaurantId?.let {
            val updatedRestaurant = restaurant.copy(
                id = it,
                timestamp = System.currentTimeMillis()
            )

            database.child("users").child(userId).child("userRestaurants").child(it)
                .setValue(updatedRestaurant)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Restaurant saved successfully")
                    } else {
                        Log.e("Firebase", "Error saving restaurant", task.exception)
                    }
                }
        }
    }
}