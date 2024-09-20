package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.MovieTile

@Composable
fun GenreDetailScreen(
    genre: String,
    moviesViewModel: GenreDetailViewModel = viewModel()
) {
    moviesViewModel.setGenre(genre)
    val movies by moviesViewModel.uiState.collectAsState()
    GenreDetailScreen(movies)
}

@Composable
fun GenreDetailScreen(
    uiState: GenreDetailUIState
) {
    when (uiState) {
        GenreDetailUIState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is GenreDetailUIState.Error -> {
            Text(uiState.errorMessage)
        }

        is GenreDetailUIState.Success -> {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = stringResource(R.string.title_genre_detail, uiState.genreName),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.title_most_popular),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                LazyRow {
                    items(uiState.mostPopular) { movie ->
                        MovieTile(
                            movieId = movie.id.toString(),
                            movieTitle = movie.title,
                            movieImageUrl = movie.imageUrl,
                            movieRating = movie.rating,
                            onMovieClicked = {
                                // TODO
                            }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.title_most_recent),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                LazyRow {
                    items(uiState.mostRecent) { movie ->
                        MovieTile(
                            movieId = movie.id.toString(),
                            movieTitle = movie.title,
                            movieImageUrl = movie.imageUrl,
                            movieRating = movie.rating,
                            onMovieClicked = {
                                // TODO(thatfiredev)
                            }
                        )
                    }
                }
            }
        }
    }
}

