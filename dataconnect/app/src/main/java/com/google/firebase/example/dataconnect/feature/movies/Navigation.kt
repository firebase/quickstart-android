package com.google.firebase.example.dataconnect.feature.movies

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val MOVIES_ROUTE = "movies_route"

fun NavController.navigateToMovies(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(MOVIES_ROUTE, navOptions)

fun NavGraphBuilder.moviesScreen(

) {
    composable(route = MOVIES_ROUTE) {
        // TODO: Add Movies composable
    }
}


