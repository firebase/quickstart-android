package com.google.firebase.example.dataconnect.feature.movies

import com.google.firebase.dataconnect.movies.MoviesRecentlyReleasedQuery
import com.google.firebase.dataconnect.movies.MoviesTop10Query

sealed class MoviesUIState {

    data object Loading: MoviesUIState()

    data class Error(val errorMessage: String): MoviesUIState()

    data class Success(
        val top10movies: List<MoviesTop10Query.Data.MoviesItem>,
        val latestMovies: List<MoviesRecentlyReleasedQuery.Data.MoviesItem>
    ) : MoviesUIState()
}
