package com.google.firebase.example.dataconnect.feature.moviedetail

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val MOVIE_DETAIL_ROUTE = "movies/{movie}"

fun NavController.navigateToMovieDetail(
    movieId: String,
    navOptions: NavOptionsBuilder.() -> Unit = { }
) = navigate(MOVIE_DETAIL_ROUTE.replace("{movie}", movieId), navOptions)

fun NavGraphBuilder.movieDetailScreen(
    onActorClicked: (actorId: String) -> Unit
) {
    composable(
        route = MOVIE_DETAIL_ROUTE
    ) { backStackEntry ->
        backStackEntry.arguments?.let {
            val movieId = it.getString("movie")
            movieId?.let { id ->
                MovieDetailScreen(id, onActorClicked)
            }

        }
    }
}


