package com.google.firebase.example.dataconnect.feature.search

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val SEARCH_ROUTE = "search"

fun NavController.navigateToSearch(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(SEARCH_ROUTE, navOptions)

fun NavGraphBuilder.searchScreen(
    // TODO(thatfiredev): handle clicks
) {
    composable(route = SEARCH_ROUTE) {
        SearchScreen()
    }
}
