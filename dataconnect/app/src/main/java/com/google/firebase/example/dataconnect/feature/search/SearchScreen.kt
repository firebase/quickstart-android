package com.google.firebase.example.dataconnect.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.example.dataconnect.feature.profile.ReviewsList
import com.google.firebase.example.dataconnect.ui.components.Actor
import com.google.firebase.example.dataconnect.ui.components.ActorsList
import com.google.firebase.example.dataconnect.ui.components.ErrorCard
import com.google.firebase.example.dataconnect.ui.components.LoadingScreen
import com.google.firebase.example.dataconnect.ui.components.Movie
import com.google.firebase.example.dataconnect.ui.components.MoviesList
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class) // For BottomSheet and FilterChip
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }
    val uiState by searchViewModel.uiState.collectAsState()

    when (uiState) {
        is SearchUIState.Error -> ErrorCard((uiState as SearchUIState.Error).errorMessage)
        SearchUIState.Loading -> LoadingScreen()
        is SearchUIState.Success -> {
            val searchResult = (uiState as SearchUIState.Success).searchResult
            Column {
                OutlinedButton(
                    onClick = {
                        showBottomSheet = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                ) {
                    Text("Try a different query")
                }
                MoviesList(
                    listTitle = "Movie Results",
                    movies = searchResult.moviesMatchingTitle.mapNotNull {
                        Movie(
                            it.id.toString(),
                            it.imageUrl,
                            it.title,
                            it.rating?.toFloat()
                        )
                    },
                    onMovieClicked =  {
                        // TODO(thatfiredev): Navigate to movie detail screen
                    }
                )
                ActorsList(
                    listTitle = "Actor Results",
                    actors = searchResult.actorsMatchingName.mapNotNull {
                        Actor(
                            it.id.toString(),
                            it.imageUrl,
                            it.name
                        )
                    },
                    onActorClicked = {
                        // TODO(thatfiredev): Navigate to actor detail screen
                    }
                )
                // TODO: Add reviews list
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            var query by remember { mutableStateOf("") }
            var minYear by remember { mutableIntStateOf(2000) }
            var maxYear by remember { mutableIntStateOf(2024) }
            var minRating by remember { mutableFloatStateOf(1f) }
            var maxRating by remember { mutableFloatStateOf(5f) }
            var selectedGenre by remember { mutableStateOf("Action") }
            val genres = listOf("Action", "Crime", "Drama", "Sci-Fi")

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                RangeSlider(
                    value = minYear.toFloat()..maxYear.toFloat(),
                    onValueChange = {
                        minYear = it.start.toInt()
                        maxYear = it.endInclusive.toInt()
                        if (minYear == maxYear) {
                            if (maxYear < 2024) {
                                maxYear++
                            } else if (minYear > 1900) {
                                minYear--
                            }
                        }
                    },
                    valueRange = 2000f..2024f,
                    steps = 24
                )
                Text(
                    text = "Year: $minYear - $maxYear",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                RangeSlider(
                    value = minRating.toFloat()..maxRating.toFloat(),
                    onValueChange = {
                        // Round the values to the nearest 0.5
                        minRating = (Math.round(it.start * 2) / 2.0).toFloat()
                        maxRating = (Math.round(it.endInclusive * 2) / 2.0).toFloat()
                        if (minRating == maxRating) {
                            if (maxRating < 5f) {
                                maxRating += 0.5f
                            } else if (minRating > 1f) {
                                minRating -= 0.5f
                            }
                        }
                    },
                    valueRange = 1f..5f,
                    steps = 9
                )
                Text(
                    "Rating: $minRating - $maxRating",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow {
                    items(genres) { genre ->
                        FilterChip(
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) },
                            selected = selectedGenre == genre,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        searchViewModel.search(query, minYear, maxYear,
                            minRating.toDouble(), maxRating.toDouble(), selectedGenre)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.6f)
                ) {
                    Text("Search")
                }
            }

        }
    }
}
