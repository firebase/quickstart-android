package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.media.imagen.BidiViewModel
import kotlinx.serialization.Serializable

@Serializable
class StreamRealtimeVideoRoute(val sampleId: String)

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun StreamRealtimeVideoScreen(bidiView: BidiViewModel = viewModel<BidiViewModel>()) {

}