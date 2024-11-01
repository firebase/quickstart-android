package com.google.firebase.example.dataconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.dataconnect.movies.MoviesConnector
import com.google.firebase.dataconnect.movies.instance
import com.google.firebase.example.dataconnect.feature.actordetail.ActorDetailRoute
import com.google.firebase.example.dataconnect.feature.actordetail.ActorDetailScreen
import com.google.firebase.example.dataconnect.feature.moviedetail.MovieDetailRoute
import com.google.firebase.example.dataconnect.feature.moviedetail.MovieDetailScreen
import com.google.firebase.example.dataconnect.feature.movies.MoviesRoute
import com.google.firebase.example.dataconnect.feature.movies.MoviesScreen
import com.google.firebase.example.dataconnect.feature.profile.ProfileRoute
import com.google.firebase.example.dataconnect.feature.profile.ProfileScreen
import com.google.firebase.example.dataconnect.feature.search.searchScreen
import com.google.firebase.example.dataconnect.ui.theme.FirebaseDataConnectTheme

data class TopLevelRoute<T : Any>(val labelResId: Int, val route: T, val icon: ImageVector)

val TOP_LEVEL_ROUTES = listOf(
    TopLevelRoute(R.string.label_movies, MoviesRoute, Icons.Filled.Home),
    TopLevelRoute(R.string.label_profile, ProfileRoute, Icons.Filled.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Comment the line below to use a production environment instead
        MoviesConnector.instance.dataConnect.useEmulator("10.0.2.2", 9399)
        setContent {
            FirebaseDataConnectTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                                val label = stringResource(topLevelRoute.labelResId)
                                NavigationBarItem(
                                    icon = { Icon(topLevelRoute.icon, contentDescription = label) },
                                    label = { Text(label) },
                                    selected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(topLevelRoute.route::class)
                                    } == true,
                                    onClick = {
                                        navController.navigate(
                                            topLevelRoute.route,
                                            { launchSingleTop = true }
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = MoviesRoute,
                        Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                    ) {
                        composable<MoviesRoute>() {
                            MoviesScreen(
                                onMovieClicked = { movieId ->
                                    navController.navigate(
                                        route = MovieDetailRoute(movieId),
                                        builder = {
                                            launchSingleTop = true
                                        }
                                    )
                                }
                            )
                        }
                        composable<MovieDetailRoute> {
                            MovieDetailScreen(
                                onActorClicked = { actorId ->
                                    navController.navigate(
                                        ActorDetailRoute(actorId),
                                        { launchSingleTop = true }
                                    )
                                }
                            )
                        }
                        composable<ActorDetailRoute>() {
                            ActorDetailScreen(
                                onMovieClicked = { movieId ->
                                    navController.navigate(
                                        MovieDetailRoute(movieId),
                                        { launchSingleTop = true }
                                    )
                                }
                            )
                        }
                        searchScreen()
                        composable<ProfileRoute> { ProfileScreen(
                            onMovieClicked = { movieId ->
                                navController.navigate(
                                    MovieDetailRoute(movieId),
                                    { launchSingleTop = true }
                                )
                            }
                        ) }
                    }
                }
            }
        }
    }
}
