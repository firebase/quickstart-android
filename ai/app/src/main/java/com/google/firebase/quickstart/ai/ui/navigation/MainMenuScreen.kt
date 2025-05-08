package com.google.firebase.quickstart.ai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.quickstart.ai.feature.live.StreamSample
import com.google.firebase.quickstart.ai.feature.media.MediaSamples
import com.google.firebase.quickstart.ai.feature.text.TextSamples
import com.google.firebase.quickstart.ai.ui.shared.Sample

// Bottom Bar destination
enum class BottomBarDestinations(
    val label: String,
    val icon: ImageVector
) {
    TextPromptsNav("Text prompts", Icons.Rounded.ChatBubbleOutline),
    MediaStudioNav("Media studio", Icons.Default.PhotoLibrary),
    StreamRealtimeNav("Stream realtime", Icons.Default.Mic),
}

@Composable
fun MainMenuScreen(
    onTextSampleClicked: (Sample) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDestination by rememberSaveable { mutableStateOf(BottomBarDestinations.TextPromptsNav) }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            BottomBarDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        },
        modifier = modifier
    ) {
        when (currentDestination) {
            BottomBarDestinations.TextPromptsNav -> {
                TextSamples(
                    onSampleClicked = {
                        onTextSampleClicked(it)
                    }
                )
            }

            BottomBarDestinations.MediaStudioNav -> {
                MediaSamples()
            }

            BottomBarDestinations.StreamRealtimeNav -> {
                StreamSample()
            }
        }
    }
}