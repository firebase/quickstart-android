package com.google.firebase.example.dataconnect.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.dataconnect.movies.GetUserByIdQuery
import com.google.firebase.dataconnect.movies.ListUsersQuery

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    when (uiState) {
        is ProfileUIState.Error -> {
            Text((uiState as ProfileUIState.Error).errorMessage)
        }

        is ProfileUIState.SignUpState -> {
            AuthScreen(
                onSignUp = { email, password, displayName ->
                    profileViewModel.signUp(email, password, displayName)
                },
                onSignIn = {email, password ->
                    profileViewModel.signIn(email, password)
                }
            )
        }

        is ProfileUIState.ProfileState -> {
            val userName = (uiState as ProfileUIState.ProfileState).username
            ProfileScreen(
                userName ?: "User",
                emptyList(),
                emptyList(),
                emptyList()
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
    favoriteMovies: List<ListUsersQuery.Data.UsersItem.FavoriteMoviesOnUserItem.Movie>,
    favoriteActors: List<ListUsersQuery.Data.UsersItem.FavoriteActorsOnUserItem.Actor>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Welcome back, $name!",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        ProfileSection(title = "Reviews", content = { ReviewsList(reviews) })
        Spacer(modifier = Modifier.height(16.dp))

        ProfileSection(title = "Favorite Movies", content = { FavoriteMoviesList(favoriteMovies) })
        Spacer(modifier = Modifier.height(16.dp))

        ProfileSection(title = "Favorite Actors", content = { FavoriteActorsList(favoriteActors) })
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ReviewsList(reviews: List<GetUserByIdQuery.Data.User.ReviewsItem>) {
    // Display the list of reviews
}

@Composable
fun FavoriteMoviesList(movies: List<ListUsersQuery.Data.UsersItem.FavoriteMoviesOnUserItem.Movie>) {
    // Display the list of favorite movies
}

@Composable
fun FavoriteActorsList(actors: List<ListUsersQuery.Data.UsersItem.FavoriteActorsOnUserItem.Actor>) {
    // Display the list of favorite actors
}

@Composable
fun AuthScreen(
    onSignUp: (email: String, password: String, displayName: String) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Name") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (isSignUp) {
                onSignUp(email, password, displayName)
            } else {
                onSignIn(email, password)
            }
        }) {
            Text(
                text= if (isSignUp) {
                    "Sign up"
                } else {
                    "Sign in"
                })
        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Button(onClick = { /* Handle Google Sign-in */ }) {
//            Text("Sign in with Google")
//        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSignUp) {
                "Already have an account?"
            } else {
                "Don't have an account?"
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            isSignUp = !isSignUp
        }) {
            Text(
                text = if (isSignUp) {
                    "Sign in"
                } else {
                    "Sign up"
                }
            )
        }
    }
}
