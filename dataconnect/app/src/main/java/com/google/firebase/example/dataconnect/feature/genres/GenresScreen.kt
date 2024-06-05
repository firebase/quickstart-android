package com.google.firebase.example.dataconnect.feature.genres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun GenresScreen(
    onGenreClicked: (genre: String) -> Unit = {}
) {
    // Hardcoding genres for now
    val genres = arrayOf("Action", "Crime", "Drama", "Sci-Fi")

    LazyColumn {
        items(genres) { genre ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .clickable {
                        onGenreClicked(genre)
                    }
            ) {
                Text(
                    text = genre,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}