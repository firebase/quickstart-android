package com.google.firebase.example.dataconnect.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUIState>(SearchUIState.Loading)
    val uiState: StateFlow<SearchUIState>
        get() = _uiState

    fun search(
        userQuery: String,
        minYear: Int,
        maxYear: Int,
        minRating: Double,
        maxRating: Double,
        genre: String
    ) {
        viewModelScope.launch {
            try {
                val result = moviesConnector.fuzzySearch.execute(
                    minYear = minYear,
                    maxYear = maxYear,
                    minRating = minRating,
                    maxRating = maxRating,
                    genre = genre,
                    {
                        input = userQuery
                    }
                )
                _uiState.value = SearchUIState.Success(result.data)
            } catch (e: Exception) {
                _uiState.value = SearchUIState.Error(e.message ?: "Unknown error")
            }
        }
    }
}