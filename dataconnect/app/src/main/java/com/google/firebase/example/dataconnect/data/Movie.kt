package com.google.firebase.example.dataconnect.data

import java.util.UUID

data class Movie(
    val id: UUID,
    val title: String,
    val imageUrl: String,
    val releaseYear: Int? = 1970,
    val genre: String? = "",
    val rating: Double? = 0.0,
    val description: String? = "",
    val tags: List<String?>? = emptyList()
)
