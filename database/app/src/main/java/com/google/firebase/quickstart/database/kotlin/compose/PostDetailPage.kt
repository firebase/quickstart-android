package com.google.firebase.quickstart.database.kotlin.compose

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Screen


@SuppressLint("RestrictedApi")
@Composable
fun PostDetailPage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel,
) {

    BackHandler {
        rootNavController.navigate(Screen.HomeScreen.route)
    }



    Scaffold(
        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                PostDetail(rootNavController, databaseProviderViewModel, authProviderViewModel)
            }
        },
    )
}

@Composable
fun PostDetail(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {
    val comment = remember { mutableStateOf("") }

    val commentId = databaseProviderViewModel.getPostID()
    val post = databaseProviderViewModel.getPostDetail(commentId!!)
    databaseProviderViewModel.initCommentLists(commentId)

    val uid = authProviderViewModel.auth.uid!!
    val commentList = databaseProviderViewModel.getCommentLists()

    Column {
        Card(
            shape = RectangleShape,
            modifier = Modifier
                .padding(5.dp, 20.dp, 5.dp, 2.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(5.dp, 2.dp, 5.dp, 2.dp)
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
                    Spacer(modifier = Modifier.width(200.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Text(
                        text = post.title.toString(),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row {
                    Text(text = post.body.toString())
                }
            }
        }
        Column(
            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = comment.value,
                    onValueChange = { comment.value = it },
                    label = { Text("Write a comment") },

                    )
                ClickableText(
                    text = AnnotatedString("POST"),
                    onClick = {
                        databaseProviderViewModel.postComment(commentId, uid, comment.value)
                        comment.value = ""
                    },
                    modifier = Modifier.padding(20.dp, 0.dp, 0.dp, 0.dp)
                )
            }
        }

        LazyColumn {
            items(commentList) { comments ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp, 5.dp, 5.dp, 0.dp),
                    border = BorderStroke(0.dp, Color.Transparent)
                ) {
                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painterResource(R.drawable.ic_action_account_circle_40),
                                contentDescription = "Firebase user",
                                contentScale = ContentScale.Crop,
                            )
                            Text(text = comments.author.toString())
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = comments.text.toString(),
                            modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 0.dp)
                        )
                    }
                }
            }
        }
    }
}