package com.example.lazy_susan.model

import androidx.annotation.StringRes

data class Cuisine(
    @StringRes val name: Int,
    val apiType: String
)