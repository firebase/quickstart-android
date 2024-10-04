package com.google.firebase.example.dataconnect.feature.movies

import com.google.firebase.dataconnect.movies.ListMoviesQuery

sealed class MoviesUIState {

    data object Loading: MoviesUIState()

    data class Error(val errorMessage: String?): MoviesUIState()

    data class Success(
        val top10movies: List<ListMoviesQuery.Data.MoviesItem>,
        val latestMovies: List<ListMoviesQuery.Data.MoviesItem>
    ) : MoviesUIState()
}
