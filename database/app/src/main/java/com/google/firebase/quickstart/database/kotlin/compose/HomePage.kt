package com.google.firebase.quickstart.database.kotlin.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Screen
import com.google.firebase.quickstart.database.kotlin.models.Post


@Composable
fun HomePage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {

    Scaffold(
        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                Home(rootNavController, databaseProviderViewModel, authProviderViewModel)
            }
        },
    )
}


@Composable
fun Home(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel,
) {

    val uid = authProviderViewModel.auth.uid!!
    val databaseref = databaseProviderViewModel.database.reference.child("posts")
    val posts = databaseProviderViewModel.getPosts(databaseref)
    posts.reverse()

    if (posts != null) {
        displayList(databaseProviderViewModel, uid, rootNavController, databaseref)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun displayList(
    databaseProviderViewModel: DatabaseProviderViewModel,
    uid: String,
    rootNavController: NavHostController,
    databaseReference: DatabaseReference
) {
    val starResourceNotLiked = R.drawable.ic_toggle_star_outline_24
    val starResourceLiked = R.drawable.ic_toggle_star_24
    var starDisplay by remember { mutableStateOf(starResourceLiked) }
    var isLike: Boolean

    val posts = databaseProviderViewModel.getPosts(databaseReference)

    Spacer(modifier = Modifier.height(5.dp))
    LazyColumn {
        items(posts) { post ->
            Card(
                shape = RectangleShape,
                elevation = 4.dp,
                modifier = Modifier
                    .padding(1.dp, 2.dp, 5.dp, 0.dp)
                    .fillMaxWidth(),
                onClick = {
                    onCardClick(rootNavController, post.key!!)
                }
            ) {
                Column(
                    Modifier
                        .padding(16.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(R.drawable.ic_action_account_circle_40),
                            contentDescription = "Firebase user",
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = post.author.toString())

                        isLike = post.stars.containsKey(uid!!)
                        starDisplay = if (isLike) {
                            starResourceLiked
                        } else {
                            starResourceNotLiked
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp, 0.dp, 10.dp, 0.dp)
                        ) {
                            Image(
                                painterResource(starDisplay),
                                contentDescription = "star",
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd,
                                modifier = Modifier.clickable {
                                    onStarClick(databaseProviderViewModel, uid, post)
                                }
                            )
                            Text(text = post.starCount.toString())
                        }

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Text(text = post.title.toString())
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Row {
                        Text(text = post.body.toString())
                    }
                }
            }
        }
    }
}

fun onStarClick(databaseProviderViewModel: DatabaseProviderViewModel, uid: String, post: Post) {

    val globalPostRef = databaseProviderViewModel.database.reference.child("posts").child(post.key!!)
    val userPostRef =
        databaseProviderViewModel.database.reference.child("user-posts").child(post.uid!!).child(post.key!!)

    databaseProviderViewModel.setStarStatus(globalPostRef, uid)
    databaseProviderViewModel.setStarStatus(userPostRef, uid)
}

@SuppressLint("RestrictedApi")
fun onCardClick(rootNavController: NavHostController, uid: String) {

    val bundle = bundleOf("uid" to uid)
    val destination = rootNavController.findDestination(Screen.CommentScreen.route)
    if (destination != null) {
        rootNavController.navigate(destination.id, bundle)
    }
}
