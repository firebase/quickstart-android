package com.google.firebase.quickstart.ai.feature.hybrid

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.ondevice.DownloadStatus
import com.google.firebase.ai.ondevice.FirebaseAIOnDevice
import com.google.firebase.ai.ondevice.OnDeviceModelStatus
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.HybridInferenceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
object HybridInferenceRoute

@OptIn(PublicPreviewAPI::class)
class HybridInferenceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HybridInferenceUiState(
        expenses = listOf(
            Expense(UUID.randomUUID().toString(), "Lunch", 15.50),
            Expense(UUID.randomUUID().toString(), "Coffee", 4.75)
        )
    ))
    val uiState: StateFlow<HybridInferenceUiState> = _uiState.asStateFlow()

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
        )

    init {
        checkAndDownloadModel()
    }

    private fun checkAndDownloadModel() {
        viewModelScope.launch {
            try {
                val status = FirebaseAIOnDevice.checkStatus()
                updateStatus(status)

                if (status == OnDeviceModelStatus.DOWNLOADABLE) {
                    FirebaseAIOnDevice.download().collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                _uiState.update { it.copy(modelStatus = "Downloading model...") }
                            }
                            is DownloadStatus.DownloadInProgress -> {
                                val progress = downloadStatus.totalBytesDownloaded
                                _uiState.update { it.copy(modelStatus = "Downloading: $progress bytes downloaded") }
                            }
                            is DownloadStatus.DownloadCompleted -> {
                                _uiState.update { it.copy(modelStatus = "Model ready") }
                            }
                            is DownloadStatus.DownloadFailed -> {
                                _uiState.update { it.copy(modelStatus = "Download failed", errorMessage = "Model download failed") }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HybridVM", "Error checking model status", e)
                _uiState.update { it.copy(modelStatus = "Error checking status", errorMessage = e.message) }
            }
        }
    }

    private fun updateStatus(status: OnDeviceModelStatus) {
        val statusText = when (status) {
            OnDeviceModelStatus.AVAILABLE -> "Model available"
            OnDeviceModelStatus.DOWNLOADABLE -> "Model downloadable"
            OnDeviceModelStatus.DOWNLOADING -> "Model downloading..."
            OnDeviceModelStatus.UNAVAILABLE -> "On-device model unavailable"
            else -> "Unknown"
        }
        _uiState.update { it.copy(modelStatus = statusText) }
    }

    fun scanReceipt(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, errorMessage = null) }
            try {
                val prompt = content {
                    image(bitmap)
                    text("Extract the store name and the total price from this receipt. Output only in CSV format like 'Store,Price'. Example: 'Starbucks,5.50'")
                }

                val response = model.generateContent(prompt)
                val text = response.text
                Log.d("HybridVM", "Response is: $text")
                if (text != null) {
                    parseAndAddExpense(text)
                } else {
                    _uiState.update { it.copy(errorMessage = "Could not extract data") }
                }
            } catch (e: Exception) {
                Log.e("HybridVM", "Error scanning receipt", e)
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    private fun parseAndAddExpense(text: String) {
        // Simple parsing: "Store, Price"
        val parts = text
            // Sometimes the output contains single quotes
            .replace("'", "")
            .split(",", limit = 2)
        if (parts.size >= 2) {
            val name = parts[0].trim()
            val priceStr = parts[1].trim()
                .replace("$", "")
                .replace(",", "")
            val price = priceStr.toDoubleOrNull() ?: 0.0
            
            val newExpense = Expense(UUID.randomUUID().toString(), name, price)
            _uiState.update { it.copy(expenses = it.expenses + newExpense) }
        } else {
            _uiState.update { it.copy(errorMessage = "Unexpected AI output format: $text") }
        }
    }
}
