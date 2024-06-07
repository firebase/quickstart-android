package com.google.firebase.example.dataconnect.feature.movies

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val MOVIES_ROUTE = "movies"

fun NavController.navigateToMovies(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(MOVIES_ROUTE, navOptions)

fun NavGraphBuilder.moviesScreen(
    onMovieClicked: (movie: String) -> Unit
) {
    composable(route = MOVIES_ROUTE) {
        MoviesScreen(onMovieClicked)
    }
}


