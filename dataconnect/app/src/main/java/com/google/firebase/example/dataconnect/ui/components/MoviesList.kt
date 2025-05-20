package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Used to represent a movie in a list UI
 */
data class Movie(
    val id: String,
    val imageUrl: String,
    val title: String,
    val rating: Float? = null
)

/**
 * Displays a scrollable horizontal list of movies.
 */
@Composable
fun MoviesList(
    modifier: Modifier = Modifier,
    listTitle: String,
    movies: List<Movie>? = emptyList(),
    onMovieClicked: (movieId: String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = listTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow {
            items(movies.orEmpty()) { movie ->
                MovieTile(
                    movie = movie,
                    onMovieClicked = {
                        onMovieClicked(movie.id.toString())
                    }
                )
            }
        }
    }
}

/**
 * Used to display each movie item in the list.
 */
@Composable
fun MovieTile(
    modifier: Modifier = Modifier,
    tileWidth: Dp = 150.dp,
    movie: Movie,
    onMovieClicked: (movieId: String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .sizeIn(maxWidth = tileWidth)
            .clickable {
                onMovieClicked(movie.id)
            },
    ) {
        AsyncImage(
            model = movie.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.aspectRatio(9f / 16f)
        )
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        movie.rating?.let {
            Text(
                text = "Rating: $it",
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}