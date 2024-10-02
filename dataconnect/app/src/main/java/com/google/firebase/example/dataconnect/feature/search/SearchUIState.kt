package com.google.firebase.example.dataconnect.feature.search

import com.google.firebase.dataconnect.movies.FuzzySearchQuery
import com.google.firebase.dataconnect.movies.MoviesRecentlyReleasedQuery
import com.google.firebase.dataconnect.movies.MoviesTop10Query

sealed class SearchUIState {

    data object Loading: SearchUIState()

    data class Error(val errorMessage: String): SearchUIState()

    data class Success(
        val searchResult: FuzzySearchQuery.Data
    ) : SearchUIState()
}
