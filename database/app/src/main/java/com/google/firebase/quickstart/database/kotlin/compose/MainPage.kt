package com.google.firebase.quickstart.database.kotlin.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.compose.navigation.contents.Screen
import com.google.firebase.quickstart.database.kotlin.compose.topbar.TabLayout

@Composable
fun MainPage(
    rootNavController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {

    if(authProviderViewModel.auth.currentUser == null){return}

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = colorResource(R.color.colorPrimary)
            ) {
                LogOutMenu(authProviderViewModel,rootNavController)
            }
        },
        content = {
            TabLayout(rootNavController,databaseProviderViewModel,authProviderViewModel)
            Column(
                modifier = Modifier.padding(it)
            ) {
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { rootNavController.navigate(Screen.NewPostScreen.route) },
                backgroundColor = Color.Cyan
            ) {
                Icon(Icons.Filled.Create, "Write post")
            }
        }
    )
}

@Composable
fun LogOutMenu(authProviderViewModel: AuthProviderViewModel,
               rootNavController: NavHostController) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = {
                showMenu = !showMenu

            }) {
                Icon(Icons.Default.MoreVert, "Settings")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(onClick = {
                    authProviderViewModel.auth.signOut()
                   rootNavController.navigate(Screen.AuthScreen.route)
                }) {
                    Text(text = "Logout")
                }
            }
        }
    )
}