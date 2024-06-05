package com.google.firebase.example.dataconnect.feature.genres

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val GENRES_ROUTE = "genres"

fun NavController.navigateToGenres(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(GENRES_ROUTE, navOptions)

fun NavGraphBuilder.genresScreen(
    onGenreClicked: (genre: String) -> Unit
) {
    composable(route = GENRES_ROUTE) {
        GenresScreen(onGenreClicked)
    }
}


