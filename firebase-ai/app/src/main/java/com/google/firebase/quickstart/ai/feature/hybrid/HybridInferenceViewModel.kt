package com.google.firebase.quickstart.ai.feature.hybrid

import android.graphics.Bitmap
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
import com.google.firebase.quickstart.ai.ui.HybridInferenceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
object HybridInferenceRoute

@OptIn(PublicPreviewAPI::class)
class HybridInferenceViewModel : ViewModel() {
    val uiState: StateFlow<HybridInferenceUiState>
        field = MutableStateFlow(
            HybridInferenceUiState(
                expenses = listOf(
                    Expense("Lunch", 15.50, "Example data"),
                    Expense("Coffee", 4.75, "Example data")
                )
            )
        )

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-3.1-flash-lite",
        onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
    )

    init {
        checkAndDownloadModel()
    }

    private fun checkAndDownloadModel() {
        viewModelScope.launch {
            try {
                val status = model.onDeviceExtension?.checkStatus()
                updateStatus(status)

                if (status == OnDeviceModelStatus.DOWNLOADABLE) {
                    model.onDeviceExtension?.download()?.collect { downloadStatus ->
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
                                        modelStatus = "Download failed", errorMessage = "Model download failed"
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

    fun scanReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            uiState.update { it.copy(isScanning = true, errorMessage = null) }
            try {
                val prompt = content {
                    image(bitmap)
                    text(
                        """
                        Extract the store name and the total price from this receipt.
                        Output only in JSON format containg 2 fields '{name,price}'.
                        Do not include any currency signs or backticks or any text around it.
                        Use dots for decimals.
                        Examples:
                        - {"name": "FakeStore", "price": "2.0"}
                        - {"name": "SomeMarket", "price": "3.5"}
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
                    parseAndAddExpense(text, inferenceMode)
                } else {
                    uiState.update { it.copy(errorMessage = "Could not extract data") }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            } finally {
                uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    private fun parseAndAddExpense(text: String, inferenceMode: String) {
        val json = text
            // The on-device model sometimes outputs backticks, so we remove those
            .replace("```json", "")
            .replace("```", "")
        try {
            val newExpense = Json.decodeFromString<Expense>(json).copy(inferenceMode = inferenceMode)
            uiState.update { it.copy(expenses = it.expenses + newExpense) }
        } catch (e: Exception) {
            uiState.update { it.copy(errorMessage = e.localizedMessage) }
        }
    }
}
