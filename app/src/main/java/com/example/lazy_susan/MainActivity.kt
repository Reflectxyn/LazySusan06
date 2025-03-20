package com.example.lazy_susan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                LazySusanApp(
                    modifier = Modifier.padding(innerPadding),
                    authViewModel = authViewModel
                )
            }
        }
    }
}

/*
package com.example.basicapi

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() { // Use ComponentActivity for simplicity

    private lateinit var selectedRestaurantTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectedRestaurantTextView = findViewById(R.id.selectedRestaurant)

        // Example address to fetch coordinates
        val address = "4551 Linden Ave, Long Beach, CA"
        ApiHelper.getCoordinates(address) { lat, lng ->
            // Once coordinates are fetched, call to get nearby restaurants
            ApiHelper.getNearbyRestaurants(lat, lng) { restaurants ->
                // Run UI-related code on the main thread
                runOnUiThread {
                    if(restaurants.isNotEmpty()){
                        val randomRestaurant = restaurants.random()
                        selectedRestaurantTextView.text = "Restaurant: ${randomRestaurant.name}" +
                                "\nAddress: ${randomRestaurant.address}" +
                                "\nPhone Number: ${randomRestaurant.phoneNumber}" +
                                "\nHours: ${randomRestaurant.hours}"
                    } else {
                        selectedRestaurantTextView.text = "No restaurants found nearby."
                    }
                }
            }
        }
    }
}




*/
