package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        GenreDetailUIState.Loading -> LoadingScreen()

        is GenreDetailUIState.Error -> ErrorCard(uiState.errorMessage)

        is GenreDetailUIState.Success -> {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = stringResource(R.string.title_genre_detail, uiState.genreName),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(8.dp)
                )
                MoviesList(
                    listTitle = stringResource(R.string.title_most_popular),
                    movies = uiState.mostPopular.mapNotNull {
                        Movie(it.id.toString(), it.imageUrl, it.title, it.rating?.toFloat())
                    },
                    onMovieClicked = {
                        // TODO
                    }
                )
                MoviesList(
                    listTitle = stringResource(R.string.title_most_recent),
                    movies = uiState.mostRecent.mapNotNull {
                        Movie(it.id.toString(), it.imageUrl, it.title, it.rating?.toFloat())
                    },
                    onMovieClicked = {
                        // TODO
                    }
                )
            }
        }
    }
}

