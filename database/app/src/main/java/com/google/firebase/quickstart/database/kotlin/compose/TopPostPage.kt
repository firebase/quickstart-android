package com.google.firebase.quickstart.database.kotlin.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun TopPostPage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {

    Scaffold(
        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                TopPostDetail(rootNavController, databaseProviderViewModel, authProviderViewModel)
            }
        },
    )
}

@Composable
fun TopPostDetail(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel,
) {

    val uid = authProviderViewModel.auth.uid!!
    val ref = databaseProviderViewModel.database.reference.child("user-posts").child(uid)
    val allPosts = databaseProviderViewModel.getPosts(ref)

    allPosts.sortByDescending { post -> post.starCount }

    if (allPosts != null) {
        displayList(databaseProviderViewModel, uid, rootNavController, ref)
    }
}
