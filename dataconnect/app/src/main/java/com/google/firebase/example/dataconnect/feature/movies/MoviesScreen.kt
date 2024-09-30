package com.google.firebase.example.dataconnect.feature.movies

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.Movie
import com.google.firebase.example.dataconnect.ui.components.MoviesList
import kotlinx.serialization.Serializable

@Serializable
object MoviesRoute

@Composable
fun MoviesScreen(
    onMovieClicked: (movie: String) -> Unit,
    moviesViewModel: MoviesViewModel = viewModel()
) {
    val movies by moviesViewModel.uiState.collectAsState()
    MoviesScreen(movies, onMovieClicked)
}

@Composable
fun MoviesScreen(
    uiState: MoviesUIState,
    onMovieClicked: (movie: String) -> Unit
) {
    when (uiState) {
        MoviesUIState.Loading -> LoadingScreen()
        is MoviesUIState.Error -> ErrorCard(uiState.errorMessage)
        is MoviesUIState.Success -> {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                MoviesList(
                    listTitle = stringResource(R.string.title_top_10_movies),
                    movies = uiState.top10movies.mapNotNull {
                        Movie(it.id.toString(), it.imageUrl, it.title, it.rating?.toFloat())
                    },
                    onMovieClicked = onMovieClicked
                )
                Spacer(modifier = Modifier.height(16.dp))
                MoviesList(
                    listTitle = stringResource(R.string.title_latest_movies),
                    movies = uiState.latestMovies.mapNotNull {
                        Movie(it.id.toString(), it.imageUrl, it.title, it.rating?.toFloat())
                    },
                    onMovieClicked = onMovieClicked
                )
            }
        }
    }
}
