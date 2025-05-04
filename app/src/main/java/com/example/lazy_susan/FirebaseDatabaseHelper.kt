package com.example.lazy_susan

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.lazy_susan.pages.AwardItem

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
    fun saveAndBlockRestaurant(userId: String, restaurant: Restaurant) {
        val restaurantId = database.child("users").child(userId).child("userRestaurants").push().key

        restaurantId?.let {
            val updatedRestaurant = restaurant.copy(
                id = it,
                isBlocked = true,
                timestamp = System.currentTimeMillis()
            )

            database.child("users").child(userId).child("userRestaurants").child(it)
                .setValue(updatedRestaurant)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Restaurant saved and blocked")
                    } else {
                        Log.e("Firebase", "Error saving and blocking restaurant", task.exception)
                    }
                }
        }
    }
    fun saveAwardsToFirebase(awards: List<AwardItem>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("awards")

        val awardMap = awards.associate { it.title to it.isUnlocked }
        dbRef.setValue(awardMap)
    }

    // Load award unlock status and apply to a list of AwardItem
    fun loadAwardsFromFirebase(
        items: MutableList<AwardItem>,
        onComplete: () -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("awards")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { awardSnapshot ->
                    val title = awardSnapshot.key
                    val isUnlocked = awardSnapshot.getValue(Boolean::class.java) ?: false
                    items.find { it.title == title }?.isUnlocked = isUnlocked
                }
                onComplete()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
                onComplete()
            }
        })
    }
}


