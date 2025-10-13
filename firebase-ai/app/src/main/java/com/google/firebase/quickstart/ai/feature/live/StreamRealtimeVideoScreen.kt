package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.BidiViewModel
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeVideoRoute(val sampleId: String)

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun StreamRealtimeVideoScreen(bidiView: BidiViewModel = viewModel<BidiViewModel>()) {
    val backgroundColor =
        MaterialTheme.colorScheme.background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("mytext")
        }
    }
}