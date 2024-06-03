package com.google.firebase.example.dataconnect.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val PROFILE_ROUTE = "profile_route"

fun NavController.navigateToProfile(navOptions: NavOptionsBuilder.() -> Unit) =
    navigate(PROFILE_ROUTE, navOptions)

fun NavGraphBuilder.profileScreen(

) {
    composable(route = PROFILE_ROUTE) {
        // TODO: Call composable
    }
}


