package com.example.lazy_susan.data

import com.example.lazy_susan.R
import com.example.lazy_susan.model.Cuisine

object DataSource {
    val cuisines = listOf(
        Cuisine(R.string.cuisine_italian),
        Cuisine(R.string.cuisine_japanese),
        Cuisine(R.string.cuisine_thai),
        Cuisine(R.string.cuisine_mexican),
        Cuisine(R.string.cuisine_indian),
        Cuisine(R.string.cuisine_chinese),
        Cuisine(R.string.cuisine_greek),
        Cuisine(R.string.cuisine_american)
    )
    val ratings = listOf("2", "3", "4", "5")
    val distances = listOf("1", "2", "5", "10", "15")
}