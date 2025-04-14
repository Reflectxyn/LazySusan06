package com.example.lazy_susan.pages

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel(){
    // Holds the selected distance (as a String), default is "2" miles.
    val selectedDistance = mutableStateOf("2")

    // Holds the selected rating threshold (as a String), default is "3".
    // For example, "3" means restaurants with a rating of 3 or higher.
    val selectedRatingThreshold = mutableStateOf("3")
    // NEW: Holds the boolean selection for cuisine options.
    // The list should have the same size as DataSource.cuisines.
    val selectedCuisineBooleans = mutableStateListOf(false, false, false, false, false, false, false, false)
}