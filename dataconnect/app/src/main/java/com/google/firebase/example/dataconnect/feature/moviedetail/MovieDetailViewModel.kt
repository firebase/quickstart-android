package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import com.google.firebase.example.dataconnect.data.toMovie
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
) : ViewModel() {
    private var movieId: String = ""

    private val _uiState = MutableStateFlow<MovieDetailUIState>(MovieDetailUIState.Loading)
    val uiState: StateFlow<MovieDetailUIState>
        get() = _uiState

    fun setMovieId(id: String) {
        movieId = id
        viewModelScope.launch {
            try {
                val movie =  moviesConnector.getMovieById.execute(
                    id = UUID.fromString(movieId)
                ).data.movie?.toMovie()
                _uiState.value = MovieDetailUIState.Success(movie)
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message ?: "")
            }
        }
    }

    fun addToFavorite() {
        viewModelScope.launch {
            try {
                moviesConnector.addFavoritedMovie.execute(UUID.fromString(movieId))
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message ?: "")
            }
        }
    }
}