package com.google.firebase.example.dataconnect.feature.genredetail

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val GENRE_DETAIL_ROUTE = "genres/{genre}"

fun NavController.navigateToGenreDetail(
    genre: String,
    navOptions: NavOptionsBuilder.() -> Unit = { }
) = navigate(GENRE_DETAIL_ROUTE.replace("{genre}", genre), navOptions)

fun NavGraphBuilder.genreDetailScreen() {
    composable(
        route = GENRE_DETAIL_ROUTE
    ) { backStackEntry ->
        backStackEntry.arguments?.let {
            GenreDetailScreen(it.getString("genre", "Action"))
        }
    }
}


