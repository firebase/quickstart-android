package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.dataconnect.movies.GetMovieByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.Actor
import com.google.firebase.example.dataconnect.ui.components.ActorsList
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.ReviewCard
import com.google.firebase.example.dataconnect.ui.components.ToggleButton
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailRoute(val movieId: String)

@Composable
fun MovieDetailScreen(
    onActorClicked: (actorId: String) -> Unit,
    movieDetailViewModel: MovieDetailViewModel = viewModel()
) {
    val uiState by movieDetailViewModel.uiState.collectAsState()
    Scaffold { padding ->
        when (uiState) {
            is MovieDetailUIState.Error -> {
                ErrorCard((uiState as MovieDetailUIState.Error).errorMessage)
            }

            MovieDetailUIState.Loading -> LoadingScreen()

            is MovieDetailUIState.Success -> {
                val ui = uiState as MovieDetailUIState.Success
                val movie = ui.movie
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    MovieInformation(
                        modifier = Modifier.padding(padding),
                        movie = movie,
                        isMovieFavorite = ui.isFavorite,
                        onFavoriteToggled = { newValue ->
                            movieDetailViewModel.toggleFavorite(newValue)
                        },
                    )
                    // Main Actors list
                    ActorsList(
                        listTitle = stringResource(R.string.title_main_actors),
                        actors = movie?.mainActors?.mapNotNull {
                            Actor(it.id.toString(), it.name, it.imageUrl)
                        },
                        onActorClicked = { onActorClicked(it) }
                    )
                    // Supporting Actors list
                    ActorsList(
                        listTitle = stringResource(R.string.title_supporting_actors),
                        actors = movie?.supportingActors?.mapNotNull {
                            Actor(it.id.toString(), it.name, it.imageUrl)
                        },
                        onActorClicked = { onActorClicked(it) }
                    )
                    UserReviews(
                        onReviewSubmitted = { rating, text ->
                            movieDetailViewModel.addRating(rating, text)
                        },
                        movie?.reviews
                    )
                }

            }
        }
    }
}

@Composable
fun MovieInformation(
    modifier: Modifier = Modifier,
    movie: GetMovieByIdQuery.Data.Movie?,
    isMovieFavorite: Boolean,
    onFavoriteToggled: (newValue: Boolean) -> Unit
) {
    if (movie == null) {
        ErrorCard(stringResource(R.string.error_movie_not_found))
    } else {
        Column(
            modifier = modifier
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.releaseYear.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Outlined.Star, "Favorite")
                Text(
                    text = movie.rating?.toString() ?: "0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Row {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .aspectRatio(9f / 16f)
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row {
                        movie.tags?.let { movieTags ->
                            movieTags.filterNotNull().forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag) },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            ToggleButton(
                iconEnabled = Icons.Filled.Favorite,
                iconDisabled = Icons.Outlined.FavoriteBorder,
                textEnabled = stringResource(R.string.button_remove_favorite),
                textDisabled = stringResource(R.string.button_favorite),
                isEnabled = isMovieFavorite,
                onToggle = onFavoriteToggled
            )
        }
    }
}
