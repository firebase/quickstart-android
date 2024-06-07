package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.example.dataconnect.data.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {
    private var movieId: String = ""

    private val _uiState = MutableStateFlow<MovieDetailUIState>(MovieDetailUIState.Loading)
    val uiState: StateFlow<MovieDetailUIState>
        get() = _uiState

    fun setMovieId(id: String) {
        movieId = id
        viewModelScope.launch {
            try {
                val movie = repository.getMovieByID(movieId)
                _uiState.value = MovieDetailUIState.Success(movie)
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message ?: "")
            }
        }
    }
}