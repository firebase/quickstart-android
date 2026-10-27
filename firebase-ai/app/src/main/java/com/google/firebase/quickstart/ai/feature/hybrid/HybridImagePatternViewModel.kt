package com.google.firebase.quickstart.ai.feature.hybrid

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceModelStatus
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.HybridImagePatternUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
object HybridImagePatternRoute

@OptIn(PublicPreviewAPI::class)
class HybridImagePatternViewModel : ViewModel() {
    val uiState: StateFlow<HybridImagePatternUiState>
        field = MutableStateFlow(HybridImagePatternUiState())

    // Used only to check the on-device model status and trigger its download.
    private val statusCheckModel = Firebase.ai(
        backend = GenerativeBackend.googleAI(),
        useLimitedUseAppCheckTokens = true
    ).generativeModel(
        modelName = MODEL_NAME,
        onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
    )

    init {
        checkAndDownloadModel()
    }

    private fun checkAndDownloadModel() {
        viewModelScope.launch {
            try {
                val status = statusCheckModel.onDeviceExtension?.checkStatus()
                updateStatus(status)

                if (status == OnDeviceModelStatus.DOWNLOADABLE) {
                    statusCheckModel.onDeviceExtension?.download()?.collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                uiState.update { it.copy(modelStatus = "Downloading model...") }
                            }

                            is DownloadStatus.DownloadInProgress -> {
                                val progress = downloadStatus.totalBytesDownloaded
                                uiState.update { it.copy(modelStatus = "Downloading: $progress bytes downloaded") }
                            }

                            is DownloadStatus.DownloadCompleted -> {
                                uiState.update { it.copy(modelStatus = "Model ready") }
                            }

                            is DownloadStatus.DownloadFailed -> {
                                uiState.update {
                                    it.copy(
                                        modelStatus = "Download failed",
                                        errorMessage = "Model download failed"
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(modelStatus = "Error checking status", errorMessage = e.message) }
            }
        }
    }

    private fun updateStatus(status: OnDeviceModelStatus?) {
        val statusText = when (status) {
            OnDeviceModelStatus.AVAILABLE -> "Model available"
            OnDeviceModelStatus.DOWNLOADABLE -> "Model downloadable"
            OnDeviceModelStatus.DOWNLOADING -> "Model downloading..."
            else -> "On-device model unavailable"
        }
        uiState.update { it.copy(modelStatus = statusText) }
    }

    fun setInferenceMode(mode: InferenceModeOption) {
        uiState.update { it.copy(selectedMode = mode) }
    }

    fun setFirstImage(bitmap: Bitmap) {
        uiState.update { it.copy(firstImage = bitmap, errorMessage = null) }
    }

    fun setSecondImage(bitmap: Bitmap) {
        uiState.update { it.copy(secondImage = bitmap, errorMessage = null) }
    }

    /**
     * Decodes the image at [uri] off the main thread, downsampling it so that its longest
     * edge is at most [MAX_IMAGE_DIMENSION] pixels, and stores it in the given [slot] (1 or 2).
     */
    fun loadImageFromUri(resolver: ContentResolver, uri: Uri, slot: Int) {
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { decodeDownsampled(resolver, uri) }
                if (bitmap == null) {
                    uiState.update { it.copy(errorMessage = "Could not decode the selected image") }
                } else if (slot == 1) {
                    setFirstImage(bitmap)
                } else {
                    setSecondImage(bitmap)
                }
            } catch (e: Exception) {
                uiState.update { it.copy(errorMessage = "Error loading image: ${e.message}") }
            }
        }
    }

    private fun decodeDownsampled(resolver: ContentResolver, uri: Uri): Bitmap? {
        // First pass: read only the bounds to compute a sample size.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }

        // Second pass: decode the downsampled bitmap.
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    fun findCommonPatterns() {
        val firstBitmap = uiState.value.firstImage
        val secondBitmap = uiState.value.secondImage
        if (firstBitmap == null || secondBitmap == null) {
            uiState.update { it.copy(errorMessage = "Please provide both images first.") }
            return
        }
        val mode = uiState.value.selectedMode.mode

        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isAnalyzing = true,
                    errorMessage = null,
                    resultText = null,
                    inferenceMode = ""
                )
            }
            try {
                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = true
                ).generativeModel(
                    modelName = MODEL_NAME,
                    onDeviceConfig = OnDeviceConfig(mode = mode)
                )

                val prompt = content {
                    image(firstBitmap)
                    image(secondBitmap)
                    text(
                        """
                        Compare these two images and find common patterns, themes, objects, or visual elements shared between both images.
                        """.trimIndent()
                    )
                }

                val response = model.generateContent(prompt)
                val text = response.text
                val inferenceMode = if (response.inferenceSource == InferenceSource.ON_DEVICE) {
                    "On-device"
                } else {
                    "Cloud"
                }
                if (text != null) {
                    uiState.update {
                        it.copy(
                            resultText = text,
                            inferenceMode = inferenceMode
                        )
                    }
                } else {
                    uiState.update { it.copy(errorMessage = "Could not find patterns from the provided images") }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(errorMessage = "Error (${mode}): ${e.message}") }
            } finally {
                uiState.update { it.copy(isAnalyzing = false) }
            }
        }
    }

    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash-lite"
        private const val MAX_IMAGE_DIMENSION = 1024
    }
}
