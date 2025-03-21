package com.example.lazy_susan

data class Restaurant(
    val name: String,
    val address: String,
    val phoneNumber: String,
    val hours: String,
    var distance: String = ""
)