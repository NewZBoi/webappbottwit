package com.example.twit

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToProcessItem(
    val id: Int? = null,   // assuming table has an id
    val url: String? = null,
    val other: JsonObject? = null // catch-all for other columns
)


