package com.google.firebase.quickstart.ai.feature.hybrid

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceModelOption
import com.google.firebase.ai.OnDeviceModelStatus
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
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
                    Expense("eggs", 2.00, "Example data"),
                    Expense("Milk", 1.00, "Example data"),
                    Expense("Potatoes", 3.00, "Example data")
                )
            )
        )

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-3.5-flash",
        generationConfig {
            thinkingConfig {
                thinkingLevel = ThinkingLevel.MEDIUM
            }
        },
        onDeviceConfig = OnDeviceConfig(
            mode = InferenceMode.PREFER_ON_DEVICE,
            modelOption = OnDeviceModelOption.PREVIEW
        )
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
                        Extract all the items and their prices from this receipt.
                        Output only in JSON format as a list of items where each item contains exactly 2 fields 'name' and 'price'.
                        Do not include any currency signs or backticks or any explanation or markdown wrappers or any text around the JSON array.
                        Use dots for decimals.
                        Example format:
                        [
                          {"name": "eggs", "price": 2.0},
                          {"name": "Milk", "price": 1.0},
                          {"name": "Potatoes", "price": 3.0}
                        ]
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
                    parseAndAddExpenses(text, inferenceMode)
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

    private fun parseAndAddExpenses(text: String, inferenceMode: String) {
        val json = text
            // The on-device model sometimes outputs backticks, so we remove those
            .replace("```json", "")
            .replace("```", "")
            .trim()
        try {
            val parsedExpenses = Json.decodeFromString<List<Expense>>(json).map {
                it.copy(inferenceMode = inferenceMode)
            }
            uiState.update { it.copy(expenses = parsedExpenses, splitResult = emptyList()) }
        } catch (e: Exception) {
            uiState.update { it.copy(errorMessage = "Parsing failed: ${e.localizedMessage}") }
        }
    }

    fun updateExpenseAssignment(index: Int, names: String) {
        uiState.update { state ->
            val updated = state.expenses.mapIndexed { i, exp ->
                if (i == index) exp.copy(assignedTo = names) else exp
            }
            state.copy(expenses = updated)
        }
    }

    fun clearSplit() {
        uiState.update { it.copy(splitResult = emptyList()) }
    }

    fun calculateSplit() {
        val currentExpenses = uiState.value.expenses
        if (currentExpenses.isEmpty()) {
            uiState.update { it.copy(errorMessage = "No items to split") }
            return
        }

        viewModelScope.launch {
            uiState.update { it.copy(isCalculatingSplit = true, errorMessage = null) }
            try {
                val itemsRepresentation = currentExpenses.joinToString("\n") { exp ->
                    "- ${exp.name}: ${exp.price} (Assigned to: ${exp.assignedTo.ifBlank { "Unassigned / Shared" }})"
                }

                val prompt = content {
                    text(
                        """
                        Based on this list of items from a receipt and who chose each item, calculate how much each person owes.
                        
                        Items:
                        $itemsRepresentation
                        
                        Rules for calculation:
                        1. If an item has multiple names assigned (e.g., comma-separated like "Alice, Bob"), split the price of that item equally among them.
                        2. If an item has no assigned names (empty/blank or "Unassigned / Shared"), split its cost equally among all unique people identified across all other items. If no people are assigned to any items at all, split the entire cost of all items equally among a single general user/group named "Shared".
                        3. Calculate the total amount for each unique person.
                        4. Build a concise breakdown explanation of what they are paying for (e.g., "eggs ($1.00) + potatoes ($1.50) + shared milk ($1.00)").
                        
                        Output only in JSON format as a list of persons where each person contains exactly 3 fields:
                        - 'name' (String)
                        - 'amount' (Double, the total amount they owe)
                        - 'breakdown' (String, a concise breakdown of how the amount was calculated)
                        
                        Do not include any currency signs or backticks or any explanation or markdown wrappers or any text around the JSON array.
                        
                        Example format:
                        [
                          {"name": "Alice", "amount": 3.5, "breakdown": "eggs ($1.00) + potatoes ($1.50) + shared milk ($1.00)"},
                          {"name": "Bob", "amount": 2.5, "breakdown": "milk ($1.00) + shared milk ($1.00) + potatoes ($0.50)"}
                        ]
                        """.trimIndent()
                    )
                }

                val response = model.generateContent(prompt)
                val text = response.text
                if (text != null) {
                    val json = text
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    val parsedSplit = Json.decodeFromString<List<PersonSplit>>(json)
                    uiState.update { it.copy(splitResult = parsedSplit) }
                } else {
                    uiState.update { it.copy(errorMessage = "Could not calculate split") }
                }
            } catch (e: Exception) {
                uiState.update { it.copy(errorMessage = "Split calculation error: ${e.message}") }
            } finally {
                uiState.update { it.copy(isCalculatingSplit = false) }
            }
        }
    }
}
