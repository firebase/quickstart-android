package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.example.dataconnect.data.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GenreDetailViewModel(
    private val dataRepository: MovieRepository = MovieRepository()
) : ViewModel() {
    private var genre = ""

    private val _uiState = MutableStateFlow<GenreDetailUIState>(GenreDetailUIState.Loading)
    val uiState: StateFlow<GenreDetailUIState>
        get() = _uiState

    // TODO(thatfiredev): Create a ViewModelFactory to set genre
    fun setGenre(genre: String) {
        this.genre = genre
        viewModelScope.launch {
            try {
                val movies = dataRepository.getMoviesByGenre(genre.lowercase())
                _uiState.value = GenreDetailUIState.Success(
                    genreName = genre,
                    moviesByGenre = movies
                )
            } catch (e: Exception) {
                _uiState.value = GenreDetailUIState.Error(e.message ?: "")
            }
        }
    }
}