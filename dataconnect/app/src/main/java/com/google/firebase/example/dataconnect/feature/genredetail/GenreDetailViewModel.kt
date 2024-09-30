package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GenreDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val genre = savedStateHandle.toRoute<GenreDetailRoute>().genre
    private val moviesConnector: MoviesConnector = MoviesConnector.instance

    private val _uiState = MutableStateFlow<GenreDetailUIState>(GenreDetailUIState.Loading)
    val uiState: StateFlow<GenreDetailUIState>
        get() = _uiState

    init {
        fetchGenre()
    }

    private fun fetchGenre() {
        viewModelScope.launch {
            try {
                val data = moviesConnector.listMoviesByGenre.execute(genre.lowercase()).data
                val mostPopular = data.mostPopular
                val mostRecent = data.mostRecent
                _uiState.value = GenreDetailUIState.Success(
                    genreName = genre,
                    mostPopular = mostPopular,
                    mostRecent = mostRecent
                )
            } catch (e: Exception) {
                _uiState.value = GenreDetailUIState.Error(e.message ?: "")
            }
        }
    }
}
