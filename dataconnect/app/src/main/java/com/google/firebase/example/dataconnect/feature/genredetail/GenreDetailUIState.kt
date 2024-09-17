package com.google.firebase.example.dataconnect.feature.genredetail

import com.google.firebase.dataconnect.movies.ListMoviesByGenreQuery

sealed class GenreDetailUIState {

    data object Loading: GenreDetailUIState()

    data class Error(val errorMessage: String): GenreDetailUIState()

    data class Success(
        val genreName: String,
        val mostPopular: List<ListMoviesByGenreQuery.Data.MostPopularItem>,
        val mostRecent: List<ListMoviesByGenreQuery.Data.MostRecentItem>
    ) : GenreDetailUIState()
}