package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.dataconnect.movies.GetActorByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.Movie
import com.google.firebase.example.dataconnect.ui.components.MoviesList
import com.google.firebase.example.dataconnect.ui.components.ToggleButton

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
                ErrorCard((uiState as ActorDetailUIState.Error).errorMessage)
            }

            ActorDetailUIState.Loading -> LoadingScreen()

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
                    MoviesList(
                        listTitle = stringResource(R.string.title_main_roles),
                        movies = ui.actor?.mainActors?.mapNotNull {
                            Movie(it.id.toString(), it.imageUrl, it.title)
                        },
                        onMovieClicked = { movieId ->
                            // TODO(thatfiredev): Support navigating to movie
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MoviesList(
                        listTitle = stringResource(R.string.title_supporting_actors),
                        movies = ui.actor?.supportingActors?.mapNotNull {
                            Movie(it.id.toString(), it.imageUrl, it.title)
                        },
                        onMovieClicked = { movieId ->
                            // TODO(thatfiredev): Support navigating to movie
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
        ErrorCard(stringResource(R.string.error_movie_not_found))
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
                Text(
                    text = actor.biography ?: stringResource(R.string.biography_not_available),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ToggleButton(
                iconEnabled = Icons.Filled.Favorite,
                iconDisabled = Icons.Outlined.FavoriteBorder,
                textEnabled = stringResource(R.string.button_remove_favorite),
                textDisabled = stringResource(R.string.button_favorite),
                isEnabled = isActorFavorite,
                onToggle = onFavoriteToggled
            )
        }
    }
}
