package com.google.firebase.example.dataconnect.feature.moviedetail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.google.firebase.example.dataconnect.ui.components.ActorTile
import com.google.firebase.example.dataconnect.ui.components.ReviewCard

@Composable
fun MovieDetailScreen(
    movieId: String,
    movieDetailViewModel: MovieDetailViewModel = viewModel()
) {
    movieDetailViewModel.setMovieId(movieId)
    val uiState by movieDetailViewModel.uiState.collectAsState()
    // TODO: Create a movie favorited toggle
    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    movieDetailViewModel.addToFavorite()
//                }
//            ) {
//                Icon(Icons.Filled.FavoriteBorder, "Favorite")
//            }
//        }
    ) { padding ->
        MovieDetailScreen(
            modifier = Modifier.padding(padding),
            uiState = uiState
        )
    }
}

@Composable
fun MovieDetailScreen(
    modifier: Modifier = Modifier,
    uiState: MovieDetailUIState
) {
    when (uiState) {
        is MovieDetailUIState.Error -> {
            ErrorMessage(uiState.errorMessage)
        }

        MovieDetailUIState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is MovieDetailUIState.Success -> {
            val movie = uiState.movie
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                MovieInformation(
                    modifier = modifier,
                    movie = movie
                )
                MainActorsList(movie?.mainActors ?: emptyList())
                SupportingActorsList(movie?.supportingActors ?: emptyList())
                UserReviews(
                    onReviewSubmitted = {

                    },
                    movie?.reviews ?: emptyList()
                )
            }

        }
    }
}

@Composable
fun MovieInformation(
    modifier: Modifier = Modifier,
    movie: GetMovieByIdQuery.Data.Movie?
) {
    if (movie == null) {
        ErrorMessage(stringResource(R.string.error_movie_not_found))
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
            Row {
                OutlinedButton(onClick = {

                }) {
                    Icon(Icons.Outlined.Check, "Watched")
                    Text("Mark as watched", modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {

                }) {
                    Icon(Icons.Outlined.Favorite, "Favorite")
                    Text("Add to Favorites", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
fun MainActorsList(
    actors: List<GetMovieByIdQuery.Data.Movie.MainActorsItem?>
) {
    Text(
        text = "Main Actors",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow {
        items(actors) { actor ->
            actor?.let {
                ActorTile(it.name, it.imageUrl)
            }
        }
    }
}

@Composable
fun SupportingActorsList(
    actors: List<GetMovieByIdQuery.Data.Movie.SupportingActorsItem?>
) {
    Text(
        text = "Supporting Actors",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow {
        items(actors) { actor ->
            actor?.let {
                ActorTile(it.name, it.imageUrl)
            }
        }
    }
}

@Composable
fun UserReviews(
    onReviewSubmitted: (String) -> Unit,
    reviews: List<GetMovieByIdQuery.Data.Movie.ReviewsItem>
) {
    var reviewText by remember { mutableStateOf("") }
    Text(
        text = "User Reviews",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Write your review") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onReviewSubmitted(reviewText) }) {
            Text("Submit Review")
        }
    }
    Column {
        // TODO(thatfiredev): Handle cases where the list is too long to display
        reviews.forEach {
            ReviewCard(
                userName = it.user.username,
                date = it.reviewDate.toString(),
                rating = it.rating?.toDouble() ?: 0.0,
                text = it.reviewText ?: ""
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String
) {
    Text(message)
}
