package com.google.firebase.quickstart.database.kotlin.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Screen

@Composable
fun NewPostPage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel = viewModel(factory = DatabaseProviderViewModel.Factory)
) {
    Scaffold(
        content = {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(it)
            ) {
                NewPost(databaseProviderViewModel)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    databaseProviderViewModel.submitPost()
                    rootNavController.navigate(Screen.HomeScreen.route)
                },
                backgroundColor = Color.Cyan
            ) {
                Icon(Icons.Filled.Check, "Upload post")
            }
        }
    )
}

@Composable
fun NewPost(
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel = viewModel(factory = AuthProviderViewModel.Factory)
) {

    val title = remember { mutableStateOf<String>("") }
    val body = remember { mutableStateOf<String>("") }
    val uid = authProviderViewModel.auth.uid!!

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = title.value,
            onValueChange = {
                title.value = it
                databaseProviderViewModel.setContent(title.value, body.value, uid)
            },
            label = { Text("Title") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = body.value,
            onValueChange = {
                body.value = it
                databaseProviderViewModel.setContent(title.value, body.value, uid)
            },
            label = { Text("Write your post") }
        )
    }
}