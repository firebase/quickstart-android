package com.google.firebase.example.dataconnect.feature.genredetail

import com.google.firebase.example.dataconnect.data.Movie
import com.google.firebase.example.dataconnect.data.MoviesByGenre

sealed class GenreDetailUIState {

    data object Loading: GenreDetailUIState()

    data class Error(val errorMessage: String): GenreDetailUIState()

    data class Success(
        val genreName: String,
        val moviesByGenre: MoviesByGenre
    ) : GenreDetailUIState()
}