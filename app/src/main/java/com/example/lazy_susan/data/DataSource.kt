package com.example.lazy_susan.data

import com.example.lazy_susan.R
import com.example.lazy_susan.model.Cuisine

object DataSource {
    val cuisines = listOf(
        Cuisine(R.string.italian_cuisine, "italian_restaurant"),
        Cuisine(R.string.japanese_cuisine, "japanese_restaurant"),
        Cuisine(R.string.thai_cuisine, "thai_restaurant"),
        Cuisine(R.string.mexican_cuisine, "mexican_restaurant"),
        Cuisine(R.string.indian_cuisine, "indian_restaurant"),
        Cuisine(R.string.chinese_cuisine, "chinese_restaurant"),
        Cuisine(R.string.greek_cuisine, "greek_restaurant"),
        Cuisine(R.string.american_cuisine, "american_restaurant")
    )
    val ratings = listOf("2", "3", "4", "5")
    val distances = listOf("1", "2", "5", "10", "15")
}