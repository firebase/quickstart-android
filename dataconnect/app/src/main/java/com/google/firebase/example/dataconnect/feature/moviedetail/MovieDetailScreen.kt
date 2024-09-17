package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.data.Movie

@Composable
fun MovieDetailScreen(
    movieId: String,
    movieDetailViewModel: MovieDetailViewModel = viewModel()
) {
    movieDetailViewModel.setMovieId(movieId)
    val uiState by movieDetailViewModel.uiState.collectAsState()
    // TODO: Create a movie favorited toggle
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    movieDetailViewModel.addToFavorite()
                }
            ) {
                Icon(Icons.Filled.FavoriteBorder, "Favorite")
            }
        }
    ) { padding ->
        MovieDetailScreen(
            modifier = Modifier.padding(padding),
            uiState = uiState
        )
    }
}

@Composable
fun MovieDetailScreen(
    modifier: Modifier = Modifier,
    uiState: MovieDetailUIState
) {
    when (uiState) {
        is MovieDetailUIState.Error -> {
            ErrorMessage(uiState.errorMessage)
        }

        MovieDetailUIState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is MovieDetailUIState.Success -> {
            val movie = uiState.movie
            MovieInformation(
                modifier = modifier,
                movie = movie
            )
        }
    }
}

@Composable
fun MovieInformation(
    modifier: Modifier = Modifier,
    movie: Movie?
) {
    if (movie == null) {
        ErrorMessage(stringResource(R.string.error_movie_not_found))
    } else {
        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Row {
                Text(
                    text = movie.releaseYear.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = movie.rating?.toString() ?: "0.0",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row {
                        movie.tags?.let { movieTags ->
                            movieTags.filterNotNull().forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag) },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = movie.description ?: stringResource(R.string.description_not_available),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String
) {
    Text(message)
}
