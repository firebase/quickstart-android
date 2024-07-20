package com.google.firebase.quickstart.database.kotlin.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun MyPostsPage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {
    Scaffold(
        content = {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(it)
            ) {
                MyPosts(rootNavController, databaseProviderViewModel,authProviderViewModel)
            }
        },
    )
}

@Composable
fun MyPosts(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel,
) {

    val uid = authProviderViewModel.auth.uid!!
    val ref = databaseProviderViewModel.database.reference.child("user-posts").child(uid)

    displayList(databaseProviderViewModel, uid, rootNavController, ref)
}