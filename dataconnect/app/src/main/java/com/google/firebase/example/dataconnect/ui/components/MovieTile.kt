package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

val MAX_MOVIE_CARD_WIDTH = 150.dp

@Composable
fun MovieTile(
    modifier: Modifier = Modifier,
    movieId: String,
    movieImageUrl: String,
    movieTitle: String,
    movieRating: Double,
    onMovieClicked: (movieId: String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 4.dp)
            .sizeIn(maxWidth = MAX_MOVIE_CARD_WIDTH)
            .clickable {
                onMovieClicked(movieId)
            },
    ) {
        AsyncImage(
            model = movieImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.aspectRatio(9f / 16f)
        )
        Text(
            text = movieTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Rating: $movieRating",
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}