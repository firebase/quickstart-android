package com.google.firebase.example.dataconnect.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.dataconnect.movies.GetUserByIdQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.Actor
import com.google.firebase.example.dataconnect.ui.components.ActorsList
import com.google.firebase.example.dataconnect.ui.components.Movie
import com.google.firebase.example.dataconnect.ui.components.MoviesList
import com.google.firebase.example.dataconnect.ui.components.ReviewCard

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    when (uiState) {
        is ProfileUIState.Error -> {
            Text((uiState as ProfileUIState.Error).errorMessage)
        }

        is ProfileUIState.AuthState -> {
            AuthScreen(
                onSignUp = { email, password, displayName ->
                    profileViewModel.signUp(email, password, displayName)
                },
                onSignIn = { email, password ->
                    profileViewModel.signIn(email, password)
                }
            )
        }

        is ProfileUIState.ProfileState -> {
            val ui = uiState as ProfileUIState.ProfileState
            ProfileScreen(
                ui.username ?: "User",
                ui.reviews,
                ui.watchedMovies,
                ui.favoriteMovies,
                ui.favoriteActors,
                onSignOut = {
                    profileViewModel.signOut()
                }
            )
        }

        ProfileUIState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfileScreen(
    name: String,
    reviews: List<GetUserByIdQuery.Data.User.ReviewsItem>,
    watchedMovies: List<GetUserByIdQuery.Data.User.WatchedItem>,
    favoriteMovies: List<GetUserByIdQuery.Data.User.FavoriteMoviesItem>,
    favoriteActors: List<GetUserByIdQuery.Data.User.FavoriteActorsItem>,
    onSignOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Welcome back, $name!",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        TextButton(
            onClick = {
                onSignOut()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Sign out")
        }
        Spacer(modifier = Modifier.height(16.dp))

        MoviesList(
            listTitle = stringResource(R.string.title_watched_movies),
            movies = watchedMovies.mapNotNull {
                Movie(it.movie.id.toString(), it.movie.imageUrl, it.movie.title, it.movie.rating?.toFloat())
            },
            onMovieClicked = {
                // TODO
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        MoviesList(
            listTitle = stringResource(R.string.title_favorite_movies),
            movies = favoriteMovies.mapNotNull {
                Movie(it.movie.id.toString(), it.movie.imageUrl, it.movie.title, it.movie.rating?.toFloat())
            },
            onMovieClicked = {
                // TODO
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        ActorsList(
            listTitle = stringResource(R.string.title_favorite_actors),
            actors = favoriteActors.mapNotNull {
                Actor(it.actor.id.toString(), it.actor.name, it.actor.imageUrl)
            },
            onActorClicked = {
                // TODO
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        ProfileSection(title = "Reviews", content = { ReviewsList(name, reviews) })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ReviewsList(
    userName: String,
    reviews: List<GetUserByIdQuery.Data.User.ReviewsItem>
) {
    Column {
        // TODO(thatfiredev): Handle cases where the list is too long to display
        reviews.forEach { review ->
            ReviewCard(
                userName = userName,
                date = review.reviewDate,
                rating = review.rating?.toDouble() ?: 0.0,
                text = review.reviewText ?: ""
            )
        }
    }
}
