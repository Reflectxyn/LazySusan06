package com.example.lazy_susan

data class Restaurant(
    val name: String,
    val address: String,
    val phoneNumber: String,
    val hours: String,
    val id: String = "",
    var isFavorited: Boolean = false,
    var isBlocked: Boolean = false,
    var distance: String = "",
    var rating: Double = 0.0,
    val latitude: Double? = null,    // ← new
    val longitude: Double? = null,    // ← new
    var types: List<String> = emptyList(),
    val timestamp: Long = 0L
)