package com.example.lazy_susan

import java.util.UUID

data class Preset(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "New Preset",
    val filters: Filters = Filters()
)

data class Filters(
    val cuisines: List<Boolean> = emptyList(),
    val ratings: List<Boolean> = emptyList(),
    val distance: Int = 0
)
