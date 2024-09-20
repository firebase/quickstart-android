package com.google.firebase.example.dataconnect.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

val ACTOR_CARD_SIZE = 64.dp

/**
 * Used to represent an actor in a list UI
 */
data class Actor(
    val id: String,
    val name: String,
    val imageUrl: String
)

/**
 * Displays a scrollable horizontal list of actors.
 */
@Composable
fun ActorsList(
    modifier: Modifier = Modifier,
    listTitle: String,
    actors: List<Actor>? = emptyList(),
    onActorClicked: (actorId: String) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = listTitle,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow {
            items(actors.orEmpty()) { actor ->
                ActorTile(actor, onActorClicked)
            }
        }
    }
}

/**
 * Used to display each actor item in the list.
 */
@Composable
fun ActorTile(
    actor: Actor,
    onActorClicked: (actorId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable {
                onActorClicked(actor.id)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .sizeIn(
                    maxWidth = 160.dp,
                    maxHeight = ACTOR_CARD_SIZE + 16.dp
                )
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = actor.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(ACTOR_CARD_SIZE)
                    .clip(CircleShape)
            )
            Text(
                text = actor.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
