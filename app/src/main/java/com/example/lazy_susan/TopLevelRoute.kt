package com.example.lazy_susan

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class TopLevelRoute<T:Any> (
    val route: T,
    @DrawableRes val icon: Int,
    var background_color: Color
) {
    fun setBackgroundColor(color: Color) {
        background_color = color
    }
}
