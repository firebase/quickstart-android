package com.google.firebase.quickstart.ai.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val MIN_CARD_SIZE = 160.dp

@Composable
fun MenuScreen(
    filterTitle: String,
    filters: List<Category>,
    samples: List<Sample>,
    onSampleClicked: (sample: Sample) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var selectedCategory by remember { mutableStateOf(filters.first()) }
        Text(text = filterTitle, style = MaterialTheme.typography.titleLarge)
        LazyRow {
            items(filters) { capability ->
                FilterChip(
                    onClick = { selectedCategory = capability },
                    label = {
                        Text(capability.label)
                    },
                    selected = selectedCategory == capability,
                    leadingIcon = if (selectedCategory == capability) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Text(
            text = "Samples",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        val filteredSamples = samples.filter {
            it.categories.contains(selectedCategory)
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(MIN_CARD_SIZE),
            modifier = Modifier
        ) {
            items(filteredSamples) { sample ->
                SampleItem(sample.title, sample.description, onItemClicked = {
                    onSampleClicked(sample)
                })
            }
        }
    }
}