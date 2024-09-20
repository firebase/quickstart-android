package com.google.firebase.example.dataconnect.feature.actordetail

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

const val ACTOR_DETAIL_ROUTE = "actors/{actor}"

fun NavController.navigateToActorDetail(
    actorId: String,
    navOptions: NavOptionsBuilder.() -> Unit = { }
) = navigate(ACTOR_DETAIL_ROUTE.replace("{actor}", actorId), navOptions)

fun NavGraphBuilder.actorDetailScreen() {
    composable(
        route = ACTOR_DETAIL_ROUTE
    ) { navBackStackEntry ->
        navBackStackEntry.arguments?.let {
            val actorId = it.getString("actor")
            actorId?.let { id ->
                ActorDetailScreen(id)
            }
        }
    }
}