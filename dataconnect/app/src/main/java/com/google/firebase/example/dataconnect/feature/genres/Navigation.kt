package com.google.firebase.example.dataconnect.feature.genres

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val GENRES_ROUTE = "genres_route"

fun NavController.navigateToGenres(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(GENRES_ROUTE, navOptions)

fun NavGraphBuilder.genresScreen(

) {
    composable(route = GENRES_ROUTE) {
        GenresScreen()
    }
}


