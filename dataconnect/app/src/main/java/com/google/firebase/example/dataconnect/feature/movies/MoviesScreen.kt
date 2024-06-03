package com.google.firebase.example.dataconnect.feature.movies

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.example.dataconnect.data.Movie

@Composable
fun MoviesScreen(
    moviesViewModel: MoviesViewModel = viewModel()
) {
    val movies by moviesViewModel.movies.collectAsState()
    MoviesScreen(movies)
}

@Composable
fun MoviesScreen(
    movies: List<Movie>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(top = 48.dp)
    ) {
        items(movies) { movie ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = "Rating: ${movie.rating}",
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }
    }
}