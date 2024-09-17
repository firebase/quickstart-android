package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

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
            .padding(4.dp)
            .clickable {
                onMovieClicked(movieId)
            },
    ) {
        AsyncImage(
            model = movieImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = movieTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )
        Text(
            text = "Rating: $movieRating",
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}