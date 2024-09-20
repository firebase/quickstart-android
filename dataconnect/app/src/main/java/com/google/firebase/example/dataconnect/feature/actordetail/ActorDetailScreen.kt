package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.google.firebase.dataconnect.movies.GetActorByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.feature.moviedetail.ErrorMessage
import com.google.firebase.example.dataconnect.ui.components.ActorTile
import com.google.firebase.example.dataconnect.ui.components.MovieTile

@Composable
fun ActorDetailScreen(
    actorId: String,
    actorDetailViewModel: ActorDetailViewModel = viewModel()
) {
    actorDetailViewModel.setActorId(actorId)
    val uiState by actorDetailViewModel.uiState.collectAsState()
    Scaffold { innerPadding ->
        when (uiState) {
            is ActorDetailUIState.Error -> {
                ErrorMessage((uiState as ActorDetailUIState.Error).errorMessage)
            }

            ActorDetailUIState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            is ActorDetailUIState.Success -> {
                val ui = uiState as ActorDetailUIState.Success
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    ActorInformation(
                        modifier = Modifier.padding(innerPadding),
                        actor = ui.actor,
                        isActorFavorite = ui.isFavorite,
                        onFavoriteToggled = {
                            actorDetailViewModel.toggleFavorite(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActorInformation(
    modifier: Modifier = Modifier,
    actor: GetActorByIdQuery.Data.Actor?,
    isActorFavorite: Boolean,
    onFavoriteToggled: (newValue: Boolean) -> Unit
) {
    if (actor == null) {
        ErrorMessage(stringResource(R.string.error_movie_not_found))
    } else {
        Column(
            modifier = modifier
                .padding(16.dp)
        ) {
            Text(
                text = actor.name,
                style = MaterialTheme.typography.headlineLarge
            )
            Row {
                AsyncImage(
                    model = actor.imageUrl,
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
                    Text(
                        text = actor.biography ?: stringResource(R.string.biography_not_available),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Spacer(modifier = Modifier.width(8.dp))
                if (isActorFavorite) {
                    FilledTonalButton(onClick = {
                        onFavoriteToggled(false)
                    }) {
                        Icon(Icons.Filled.Favorite, "Favorite")
                        Text("Favorite", modifier = Modifier.padding(start = 4.dp))
                    }
                } else {
                    OutlinedButton(onClick = {
                        onFavoriteToggled(true)
                    }) {
                        Icon(Icons.Outlined.FavoriteBorder, "Favorite")
                        Text("Add to Favorites", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            MainRoles(
                movies = actor.mainActors,
                onMovieClicked = { movieId ->
                    // TODO(thatfiredev): Support navigating to movie
                }
            )
            SupportingRoles(
                movies = actor.supportingActors,
                onMovieClicked = { movieId ->
                    // TODO(thatfiredev): Support navigating to movie
                }
            )
        }
    }
}

@Composable
fun MainRoles(
    movies: List<GetActorByIdQuery.Data.Actor.MainActorsItem?>,
    onMovieClicked: (movieId: String) -> Unit
) {
    Text(
        text = "Main Roles",
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyRow {
        items(movies) { movie ->
            movie?.let {
                MovieTile(
                    movieId = it.id.toString(),
                    movieTitle = it.title,
                    movieImageUrl = it.imageUrl,
                    tileWidth = 120.dp,
                    onMovieClicked = onMovieClicked,
                )
            }
        }
    }
}

@Composable
fun SupportingRoles(
    movies: List<GetActorByIdQuery.Data.Actor.SupportingActorsItem?>,
    onMovieClicked: (movieId: String) -> Unit
) {
    Text(
        text = "Supporting Roles",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyRow(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        items(movies) { movie ->
            movie?.let {
                MovieTile(
                    movieId = it.id.toString(),
                    movieTitle = it.title,
                    movieImageUrl = it.imageUrl,
                    tileWidth = 120.dp,
                    onMovieClicked = onMovieClicked,
                )
            }
        }
    }
}
