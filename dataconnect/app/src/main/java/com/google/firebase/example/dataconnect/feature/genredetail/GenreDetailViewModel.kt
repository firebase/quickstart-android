package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import com.google.firebase.example.dataconnect.data.MoviesByGenre
import com.google.firebase.example.dataconnect.data.toMovie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GenreDetailViewModel(
    private val moviesConnector: MoviesConnector = MoviesConnector.instance
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
                val data = moviesConnector.listMoviesByGenre.execute(genre.lowercase()).data
                val mostPopular = data.mostPopular.map { it.toMovie() }
                val mostRecent = data.mostRecent.map { it.toMovie() }
                val movies = MoviesByGenre(mostPopular, mostRecent)
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