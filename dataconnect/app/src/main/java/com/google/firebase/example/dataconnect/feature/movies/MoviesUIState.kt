package com.google.firebase.example.dataconnect.feature.movies

import com.google.firebase.example.dataconnect.data.Movie

sealed class MoviesUIState {

    data object Loading: MoviesUIState()

    data class Error(val errorMessage: String): MoviesUIState()

    data class Success(
        val top10movies: List<Movie>,
        val latestMovies: List<Movie>
    ) : MoviesUIState()
}