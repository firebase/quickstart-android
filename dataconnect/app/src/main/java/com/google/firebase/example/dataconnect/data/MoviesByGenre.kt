package com.google.firebase.example.dataconnect.data

data class MoviesByGenre(
    val mostPopular: List<Movie>,
    val mostRecent: List<Movie>
)
