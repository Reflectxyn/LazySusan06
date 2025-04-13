package com.example.lazy_susan

import java.util.UUID

data class Preset(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "New Preset",
    var content: String = ""
)
