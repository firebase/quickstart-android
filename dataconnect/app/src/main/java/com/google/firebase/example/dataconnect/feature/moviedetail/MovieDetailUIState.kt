package com.google.firebase.example.dataconnect.feature.moviedetail

import com.google.firebase.example.dataconnect.data.Movie

sealed class MovieDetailUIState {
    data object Loading: MovieDetailUIState()

    data class Error(val errorMessage: String): MovieDetailUIState()

    data class Success(
        // Movie is null if it can't be found on the DB
        val movie: Movie?
    ) : MovieDetailUIState()
}
