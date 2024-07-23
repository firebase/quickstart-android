package com.google.firebase.quickstart.database.kotlin.compose.navigation.contents



open class Screen(val route: String) {

    object AuthScreen: Screen("auth_screen")
    object HomeScreen: Screen("home_screen")
    object NewPostScreen: Screen("new_post_screen")
    object CommentScreen: Screen("comment_section")
}