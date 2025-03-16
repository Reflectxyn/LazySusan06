package com.example.lazy_susan

import androidx.annotation.DrawableRes

data class TopLevelRoute<T:Any> (val route: T, @DrawableRes val icon: Int)
