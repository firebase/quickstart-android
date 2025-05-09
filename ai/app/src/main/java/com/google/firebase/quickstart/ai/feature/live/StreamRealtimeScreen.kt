package com.google.firebase.quickstart.ai.feature.live

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeRoute(val sampleId: String)

@Composable
fun StreamRealtimeScreen() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Coming soon")
    }
}
