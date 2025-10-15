package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.BidiViewModel
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeVideoRoute(val sampleId: String)

@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
@Composable
fun StreamRealtimeVideoScreen(bidiView: BidiViewModel = viewModel<BidiViewModel>()) {
    val backgroundColor =
        MaterialTheme.colorScheme.background

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CameraView(
                modifier = Modifier.fillMaxHeight(0.5f),
                onFrameCaptured = { byteArray ->
                    Log.d("CameraFeed", "Captured frame size: ${byteArray.size}")
                }
            )
        }
    }
}