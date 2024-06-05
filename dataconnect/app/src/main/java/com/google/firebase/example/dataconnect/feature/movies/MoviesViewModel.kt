package com.google.firebase.example.dataconnect.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.example.dataconnect.data.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val dataRepository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUIState>(MoviesUIState.Loading)
    val uiState: StateFlow<MoviesUIState>
        get() = _uiState

    init {
        viewModelScope.launch {
            try {
                val top10Movies = dataRepository.getTop10Movies()
                val latestMovies = dataRepository.getRecentlyReleasedMovies()

                _uiState.value = MoviesUIState.Success(top10Movies, latestMovies)
            } catch (e: Exception) {
                _uiState.value = MoviesUIState.Error(e.localizedMessage)
            }
        }
    }
}