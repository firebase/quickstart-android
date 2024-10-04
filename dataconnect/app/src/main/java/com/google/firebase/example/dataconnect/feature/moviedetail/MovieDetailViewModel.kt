package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.execute
import com.google.firebase.dataconnect.movies.instance
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val movieDetailRoute = savedStateHandle.toRoute<MovieDetailRoute>()
    private val movieId: String = movieDetailRoute.movieId

    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val moviesConnector: MoviesConnector = MoviesConnector.instance

    private val _uiState = MutableStateFlow<MovieDetailUIState>(MovieDetailUIState.Loading)
    val uiState: StateFlow<MovieDetailUIState>
        get() = _uiState

    init {
        fetchMovie()
    }

    private fun fetchMovie() {
        viewModelScope.launch {
            try {
                val user = firebaseAuth.currentUser
                val movie = moviesConnector.getMovieById.execute(
                    id = UUID.fromString(movieId)
                ).data.movie

                _uiState.value = if (user == null) {
                    MovieDetailUIState.Success(movie, isUserSignedIn = false)
                } else {
                    val isFavorite = moviesConnector.getIfFavoritedMovie.execute(
                        movieId = UUID.fromString(movieId)
                    ).data.favorite_movie != null

                    MovieDetailUIState.Success(
                        movie = movie,
                        isUserSignedIn = true,
                        isFavorite = isFavorite
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message)
            }
        }
    }

    fun toggleFavorite(newValue: Boolean) {
        // TODO(thatfiredev): hide the button if there's no user logged in
        val uid = firebaseAuth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                if (newValue) {
                    moviesConnector.addFavoritedMovie.execute(UUID.fromString(movieId))
                } else {
                    moviesConnector.deleteFavoritedMovie.execute(
                        movieId = UUID.fromString(movieId)
                    )
                }
                // Re-run the query to fetch movie
                fetchMovie()
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message)
            }
        }
    }

    fun addRating(rating: Float, text: String) {
        // TODO(thatfiredev): hide the button if there's no user logged in
        if (firebaseAuth.currentUser?.uid == null) return
        viewModelScope.launch {
            try {
                moviesConnector.addReview.execute(
                    movieId = UUID.fromString(movieId),
                    rating = rating.roundToInt(),
                    reviewText = text
                )
                // Re-run the query to fetch movie
                fetchMovie()
            } catch (e: Exception) {
                _uiState.value = MovieDetailUIState.Error(e.message)
            }
        }
    }
}