package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.serialization.Serializable


@Serializable
data class ActorDetailRoute(val actorId: String)

@Composable
fun ActorDetailScreen(
    actorDetailViewModel: ActorDetailViewModel = viewModel(),
    onMovieClicked: (actorId: String) -> Unit
) {
    val uiState by actorDetailViewModel.uiState.collectAsState()
    ActorDetailScreen(
        uiState = uiState,
        onMovieClicked = onMovieClicked
    )
}

@Composable
fun ActorDetailScreen(
    uiState: ActorDetailUIState,
    onMovieClicked: (actorId: String) -> Unit
) {
    when (uiState) {
        is ActorDetailUIState.Error -> ErrorCard(uiState.errorMessage)

        is ActorDetailUIState.Loading -> LoadingScreen()

        is ActorDetailUIState.Success -> {
            Scaffold { innerPadding ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                ) {
                    ActorInformation(
                        actor = uiState.actor,
                    )
                    MoviesList(
                        listTitle = stringResource(R.string.title_main_roles),
                        movies = uiState.actor?.mainActors?.mapNotNull {
                            Movie(it.id.toString(), it.imageUrl, it.title)
                        },
                        onMovieClicked = onMovieClicked
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MoviesList(
                        listTitle = stringResource(R.string.title_supporting_actors),
                        movies = uiState.actor?.supportingActors?.mapNotNull {
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
    actor: GetActorByIdQuery.Data.Actor?
) {
    if (actor == null) {
        ErrorCard(stringResource(R.string.error_actor_not_found))
    } else {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = actor.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .padding(vertical = 8.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = actor.name,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
