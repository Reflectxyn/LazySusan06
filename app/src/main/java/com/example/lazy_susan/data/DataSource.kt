package com.example.lazy_susan.data

import com.example.lazy_susan.R
import com.example.lazy_susan.model.Cuisine

object DataSource {
    val cuisines = listOf(
        Cuisine(R.string.italian_cuisine),
        Cuisine(R.string.japanese_cuisine),
        Cuisine(R.string.thai_cuisine),
        Cuisine(R.string.mexican_cuisine),
        Cuisine(R.string.indian_cuisine),
        Cuisine(R.string.chinese_cuisine),
        Cuisine(R.string.greek_cuisine),
        Cuisine(R.string.american_cuisine)
    )
    val ratings = listOf("2", "3", "4", "5")
    val distances = listOf("1", "2", "5", "10", "15")
}