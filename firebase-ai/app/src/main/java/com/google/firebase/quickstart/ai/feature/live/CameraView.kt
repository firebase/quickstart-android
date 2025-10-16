package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.time.Duration.Companion.seconds

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onFrameCaptured: (Bitmap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(
                        lifecycleOwner,
                        previewView,
                        cameraProvider,
                        cameraSelector,
                        onFrameCaptured,
                    )
                },
                executor,
            )
            previewView
        },
        modifier = modifier,
    )
}

private fun bindPreview(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraSelector: CameraSelector,
    onFrameCaptured: (Bitmap) -> Unit,
) {
    val preview =
        Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

    val imageAnalysis =
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(previewView.context),
                    SnapshotFrameAnalyzer(onFrameCaptured),
                )
            }

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
}

// Calls the [onFrameCaptured] callback with the captured frame every second.
private class SnapshotFrameAnalyzer(private val onFrameCaptured: (Bitmap) -> Unit) :
    ImageAnalysis.Analyzer {
    private var lastFrameTimestamp = 0L
    private val interval = 1.seconds // 1 second

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (lastFrameTimestamp == 0L) {
            lastFrameTimestamp = currentTimestamp
        }

        if (currentTimestamp - lastFrameTimestamp >= interval.inWholeMilliseconds) {
            onFrameCaptured(image.toBitmap())
            lastFrameTimestamp = currentTimestamp
        }
        image.close()
    }
}
