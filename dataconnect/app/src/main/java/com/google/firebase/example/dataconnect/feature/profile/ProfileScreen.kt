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
import com.google.firebase.dataconnect.movies.GetCurrentUserQuery
import com.google.firebase.example.dataconnect.R
import com.google.firebase.example.dataconnect.ui.components.Actor
import com.google.firebase.example.dataconnect.ui.components.ActorsList
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.Movie
import com.google.firebase.example.dataconnect.ui.components.MoviesList
import com.google.firebase.example.dataconnect.ui.components.ReviewCard
import kotlinx.serialization.Serializable

@Serializable
object ProfileRoute

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onMovieClicked: (String) -> Unit,
) {
    val uiState by profileViewModel.uiState.collectAsState()
    when (uiState) {
        is ProfileUIState.Error -> {
            ErrorCard((uiState as ProfileUIState.Error).errorMessage)
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
                ui.reviews.orEmpty(),
                ui.favoriteMovies.orEmpty(),
                onMovieClicked = onMovieClicked,
                onSignOut = {
                    profileViewModel.signOut()
                }
            )
        }

        ProfileUIState.Loading -> LoadingScreen()
    }
}

@Composable
fun ProfileScreen(
    name: String,
    reviews: List<GetCurrentUserQuery.Data.User.ReviewsItem>,
    favoriteMovies: List<GetCurrentUserQuery.Data.User.FavoriteMoviesItem>,
    onMovieClicked: (String) -> Unit,
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
            listTitle = stringResource(R.string.title_favorite_movies),
            movies = favoriteMovies.mapNotNull {
                Movie(it.movie.id.toString(), it.movie.imageUrl, it.movie.title, it.movie.rating?.toFloat())
            },
            onMovieClicked = onMovieClicked
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
    reviews: List<GetCurrentUserQuery.Data.User.ReviewsItem>
) {
    Column {
        // TODO(thatfiredev): Handle cases where the list is too long to display
        reviews.forEach { review ->
            ReviewCard(
                userName = userName,
                date = review.reviewDate,
                rating = review.rating?.toDouble() ?: 0.0,
                text = review.reviewText ?: "",
                movieName = review.movie.title
            )
        }
    }
}
