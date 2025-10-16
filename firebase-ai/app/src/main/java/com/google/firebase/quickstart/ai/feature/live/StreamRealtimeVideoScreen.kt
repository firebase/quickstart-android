package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.BidiViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeVideoRoute(val sampleId: String)

@RequiresPermission(allOf = [Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA])
@Composable
fun StreamRealtimeVideoScreen(bidiView: BidiViewModel = viewModel<BidiViewModel>()) {
    val backgroundColor =
        MaterialTheme.colorScheme.background

    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        scope.launch {
            bidiView.startConversation()
        }
        onDispose {
            bidiView.endConversation()
        }
    }

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
                onFrameCaptured = { bitmap ->
                    bidiView.sendVideoFrame(bitmap)
                }
            )
        }
    }
}