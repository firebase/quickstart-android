package com.google.firebase.quickstart.database.kotlin.compose.topbar

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.quickstart.database.kotlin.compose.AuthProviderViewModel
import com.google.firebase.quickstart.database.kotlin.compose.DatabaseProviderViewModel
import com.google.firebase.quickstart.database.kotlin.compose.HomePage
import com.google.firebase.quickstart.database.kotlin.compose.MyPostsPage
import com.google.firebase.quickstart.database.kotlin.compose.TopPostPage
import com.google.firebase.quickstart.database.kotlin.compose.flowcontrol.LoadDataStatus


@Composable
fun TabLayout(
    navController: NavHostController,
    databaseProviderViewModel: DatabaseProviderViewModel,
    authProviderViewModel: AuthProviderViewModel
) {

    val dataFlow = databaseProviderViewModel.dataLoadFlow.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("Recent", "My Post", "My Top Posts")

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(5.dp))
        ShowProgressBar(dataFlow = dataFlow)
        Spacer(modifier = Modifier.height(10.dp))
        TabRow(
            selectedTabIndex = tabIndex,
            backgroundColor = Color.White,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> {
                HomePage(navController, databaseProviderViewModel, authProviderViewModel)
            }

            1 -> {
                MyPostsPage(navController, databaseProviderViewModel, authProviderViewModel)
            }

            2 -> {
                TopPostPage(navController, databaseProviderViewModel, authProviderViewModel)
            }
        }
    }
}

@Composable
fun ShowProgressBar(dataFlow: State<LoadDataStatus<String>?>) {

    dataFlow?.value?.let { status ->
        when (status) {
            is LoadDataStatus.Failed -> {
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    Toast.makeText(context, status.exception.message, Toast.LENGTH_LONG).show()

                }
            }

            is LoadDataStatus.Loaded -> {}
            LoadDataStatus.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center

                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

    }
}
