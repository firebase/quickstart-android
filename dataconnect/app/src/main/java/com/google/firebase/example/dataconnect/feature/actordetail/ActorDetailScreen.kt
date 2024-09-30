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
import kotlinx.serialization.Serializable


@Serializable
data class ActorDetailRoute(val actorId: String)

@Composable
fun ActorDetailScreen(
    actorDetailViewModel: ActorDetailViewModel = viewModel()
) {
    val uiState by actorDetailViewModel.uiState.collectAsState()
    ActorDetailScreen(
        uiState = uiState,
        onMovieClicked = {
            // TODO
        },
        onFavoriteToggled = {
            actorDetailViewModel.toggleFavorite(it)
        }
    )
}

@Composable
fun ActorDetailScreen(
    uiState: ActorDetailUIState,
    onMovieClicked: (actorId: String) -> Unit,
    onFavoriteToggled: (newValue: Boolean) -> Unit
) {
    when (uiState) {
        is ActorDetailUIState.Error -> ErrorCard(uiState.errorMessage)

        ActorDetailUIState.Loading -> LoadingScreen()

        is ActorDetailUIState.Success -> {
            val ui = uiState
            Scaffold { innerPadding ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                ) {
                    ActorInformation(
                        actor = ui.actor,
                        isActorFavorite = ui.isFavorite,
                        onFavoriteToggled = onFavoriteToggled
                    )
                    MoviesList(
                        listTitle = stringResource(R.string.title_main_roles),
                        movies = ui.actor?.mainActors?.mapNotNull {
                            Movie(it.id.toString(), it.imageUrl, it.title)
                        },
                        onMovieClicked = onMovieClicked
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MoviesList(
                        listTitle = stringResource(R.string.title_supporting_actors),
                        movies = ui.actor?.supportingActors?.mapNotNull {
                            Movie(it.id.toString(), it.imageUrl, it.title)
                        },
                        onMovieClicked = onMovieClicked
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
        ErrorCard(stringResource(R.string.error_actor_not_found))
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
