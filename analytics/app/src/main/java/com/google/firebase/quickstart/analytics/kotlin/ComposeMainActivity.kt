package com.google.firebase.quickstart.analytics.kotlin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.analytics.R
import com.google.firebase.quickstart.analytics.kotlin.ui.theme.FirebaseAnalyticsTheme
import kotlinx.coroutines.launch

class ComposeMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FirebaseAnalyticsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainAppView()
                }
            }
        }
    }
}

@Composable
fun MainAppView(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val analyticsViewModel: FirebaseAnalyticsViewModel = viewModel(
        factory = FirebaseAnalyticsViewModel.Factory(Firebase.analytics, sharedPreferences)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                recordImageView(analyticsViewModel, context)
            } else if (event == Lifecycle.Event.ON_RESUME) {
                recordScreenView(analyticsViewModel, context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load favorite food on initial composition
    LaunchedEffect(Unit) {
        if(analyticsViewModel.userFavoriteFood.value == null)
            analyticsViewModel.showFavoriteFoodDialog.value = true
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainAppBar(analyticsViewModel)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                MainContent(analyticsViewModel)
                FavoriteFoodDialog(analyticsViewModel, snackbarHostState)
            }
        }
    )
}

@Composable
fun MainAppBar(analyticsViewModel: FirebaseAnalyticsViewModel) {
    val context = LocalContext.current
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
        backgroundColor = colorResource(R.color.colorPrimary),
        actions = {
            IconButton(onClick = {
                val imageTitle = getCurrentImageTitle(context, analyticsViewModel.selectedImageIndex.value)
                val text = "I'd love you to hear about $imageTitle"

                ShareCompat.IntentBuilder(context).setType("text/plain")
                    .setText(text)
                    .startChooser()

                analyticsViewModel.recordShareEvent(imageTitle, text)
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(analyticsViewModel: FirebaseAnalyticsViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { Constants.IMAGE_INFOS.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.targetPage }.collect { page ->
            analyticsViewModel.setSelectedImageIndex(page)
            recordImageView(analyticsViewModel, context)
            recordScreenView(analyticsViewModel, context)
        }
    }

    TabRow(
        backgroundColor = Color.White,
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                color = colorResource(id = R.color.colorPrimary)
            )
        },
    ) {
        Constants.IMAGE_INFOS.forEachIndexed { index, info ->
            val isSelected = pagerState.currentPage == index
            Tab(
                text = {
                    Text(text = stringResource(id = info.title),
                        color = if(isSelected) colorResource(id = R.color.colorPrimary) else Color.Black)
                },
                selected = isSelected,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
    //Image Pager
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        ImageCard(imageInfo = Constants.IMAGE_INFOS[page])
    }
}

@Composable
fun ImageCard(imageInfo: ImageInfo) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageInfo.image),
            contentDescription = stringResource(id = imageInfo.title),
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, shape = CircleShape)
                .padding(16.dp)
        )
    }
}

@Composable
fun FavoriteFoodDialog(
    analyticsViewModel: FirebaseAnalyticsViewModel,
    snackbarHostState: SnackbarHostState
) {
    if (analyticsViewModel.showFavoriteFoodDialog.value) {
        val coroutineScope = rememberCoroutineScope()
        val choices = stringArrayResource(id = R.array.food_items)
        var selectedItem by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = {
                analyticsViewModel.showFavoriteFoodDialog.value = false
            },
            title = { Text(stringResource(id = R.string.food_dialog_title)) },
            text = {
                Column {
                    choices.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedItem = index }
                        ) {
                            RadioButton(
                                selected = selectedItem == index,
                                onClick = { selectedItem = index }
                            )
                            Text(item)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val selectedFood = choices[selectedItem]
                    analyticsViewModel.setUserFavoriteFood(selectedFood)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Favorite Food selected: $selectedFood")
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun getCurrentImageTitle(context: Context, position: Int): String {
    val imageDetails = Constants.IMAGE_INFOS[position]
    return context.getString(imageDetails.title)
}

private fun getCurrentImageId(context: Context, position: Int): String {
    val imageDetails = Constants.IMAGE_INFOS[position]
    return context.getString(imageDetails.id)
}

private fun recordScreenView(analyticsViewModel: FirebaseAnalyticsViewModel, context: Context) {
    val position = analyticsViewModel.selectedImageIndex.value
    val imageId = getCurrentImageId(context, position)
    val imageTitle = getCurrentImageTitle(context, position)

    analyticsViewModel.recordScreenView("${imageId}-${imageTitle}", "ComposeMainActivity")
}

private fun recordImageView(analyticsViewModel: FirebaseAnalyticsViewModel, context: Context) {
    val position = analyticsViewModel.selectedImageIndex.value
    val imageId = getCurrentImageId(context, position)
    val imageTitle = getCurrentImageTitle(context, position)

    analyticsViewModel.recordImageView(imageId, imageTitle)
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    FirebaseAnalyticsTheme {
        MainAppView()
    }
}