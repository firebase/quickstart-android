package com.google.firebase.quickstart.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.quickstart.ai.ui.theme.FirebaseAIServicesTheme

@Composable
fun LandingScreen(
//    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        var selected by remember { mutableStateOf("Text") }
        val capabilities = listOf("Text", "Image", "Video", "Audio", "Document", "Function calling")
        Text("Filter by use case:")
        LazyRow {
            items(capabilities) { capability ->
                FilterChip(
                    onClick = { selected = capability },
                    label = {
                        Text(capability)
                    },
                    selected = selected == capability,
                    leadingIcon = if (selected == capability) {
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
        val menuItems = listOf(
            MenuItem("Blog post creator", "Create a blog post"),
            MenuItem("Describe video content", "Get a description of the contents of a video"),
            MenuItem("Audio diarization", "Segment and audio record by speaker labels"),
            MenuItem("Video QA - exercise", "Get the activity that's being performed in an exercise video by asking a question"),
            MenuItem("Generate avatars", "Use Imagen 3 to generate cool avatars for social media profiles"),
            MenuItem("Function calling", "Ask Gemini about the current weather"),
            MenuItem("Live API", "Have a live conversation with the Gemini model"),
        )
        Text(text="Samples",modifier = Modifier.padding(top = 16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
        ) {
            items(menuItems) { item ->
                SampleItem(item)
            }
        }
    }
}

data class MenuItem(
    val titleResId: String,
    val descriptionResId: String
)

@Composable
fun SampleItem(menuItem: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxSize()
        ) {
            Text(
//                text = stringResource(menuItem.titleResId),
                text = menuItem.titleResId,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
//                text = stringResource(menuItem.descriptionResId),
                text = menuItem.descriptionResId,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
//            TextButton(
//                onClick = {
////                    onItemClicked(menuItem.routeId)
//                },
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Text(text = stringResource(R.string.action_try))
//            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    FirebaseAIServicesTheme {
        LandingScreen()
    }
}