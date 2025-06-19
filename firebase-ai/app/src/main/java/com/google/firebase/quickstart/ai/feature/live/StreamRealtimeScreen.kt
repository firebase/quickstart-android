package com.google.firebase.quickstart.ai.feature.live

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeRoute(val sampleId: String)

@Composable
fun StreamRealtimeScreen(
    streamViewModel: StreamRealtimeViewModel = viewModel<StreamRealtimeViewModel>()
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = {
            streamViewModel.connect()
        }) {
            Text("Connect")
        }
        Button(onClick = {
            streamViewModel.start()
        }) {
            Text("Start")
        }
    }
}
