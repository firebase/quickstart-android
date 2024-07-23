package com.google.firebase.quickstart.database.kotlin.compose.navigation.contents

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.quickstart.database.kotlin.compose.AuthProviderViewModel
import com.google.firebase.quickstart.database.kotlin.compose.DatabaseProviderViewModel
import com.google.firebase.quickstart.database.kotlin.compose.LoginPage
import com.google.firebase.quickstart.database.kotlin.compose.MainPage
import com.google.firebase.quickstart.database.kotlin.compose.NewPostPage
import com.google.firebase.quickstart.database.kotlin.compose.PostDetailPage
import com.google.firebase.quickstart.database.kotlin.theme.RealtimeDatabaseTheme

@Composable
fun Navigation(
    startDestination: String = Screen.AuthScreen.route,
) {
    val rootNavController = rememberNavController()

    val databaseProviderViewModel: DatabaseProviderViewModel = viewModel(factory = DatabaseProviderViewModel.Factory)
    val authProviderViewModel: AuthProviderViewModel = viewModel(factory = AuthProviderViewModel.Factory)

    RealtimeDatabaseTheme(
    ) {
        NavHost(
            navController = rootNavController,
            startDestination = startDestination,
        ) {

            composable(route = Screen.HomeScreen.route) {
                MainPage(rootNavController = rootNavController, databaseProviderViewModel, authProviderViewModel )
            }

            composable(route = Screen.AuthScreen.route) {
                LoginPage(rootNavController = rootNavController, databaseProviderViewModel, authProviderViewModel)
            }

            composable(route = Screen.NewPostScreen.route) {
                NewPostPage(rootNavController = rootNavController, databaseProviderViewModel, authProviderViewModel)
            }

            composable(route = Screen.CommentScreen.route){
                PostDetailPage(rootNavController= rootNavController, databaseProviderViewModel, authProviderViewModel)
            }

        }

    }
}