package com.google.firebase.quickstart.ai.feature.live

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.quickstart.ai.feature.live.BidiViewModel
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

    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    DisposableEffect(hasPermissions) {
        if (hasPermissions) {
            scope.launch {
                bidiView.startConversation()
            }
        }
        onDispose {
            bidiView.endConversation()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (hasPermissions) {
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
            } else {
                Text("Camera and audio permissions are required to use this feature.")
            }
        }
    }
}