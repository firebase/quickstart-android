package com.google.firebase.quickstart.database.kotlin.compose.navigation.contents

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    RealtimeDatabaseTheme(
    ) {
        NavHost(
            navController = rootNavController,
            startDestination = startDestination,
        ) {

            composable(route = Screen.HomeScreen.route) {
                MainPage(rootNavController = rootNavController)
            }

            composable(route = Screen.AuthScreen.route) {
                LoginPage(rootNavController = rootNavController)
            }

            composable(route = Screen.NewPostScreen.route) {
                //val parentViewModel = hiltViewModel<MainViewModel>()
                NewPostPage(rootNavController = rootNavController)
            }

            composable(route = Screen.CommentScreen.route){
                PostDetailPage(rootNavController= rootNavController)
            }

        }

    }
}