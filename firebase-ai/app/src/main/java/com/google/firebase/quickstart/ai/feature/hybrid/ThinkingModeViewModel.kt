package com.google.firebase.quickstart.ai.feature.hybrid

import android.util.Log
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
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.google.firebase.quickstart.ai.ui.ThinkingModeUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ThinkingModeRoute

enum class ThinkingOption(val label: String, val enableThinking: Boolean?) {
    ENABLED("Enabled (true)", true),
    DISABLED("Disabled (false)", false),
    DEFAULT("Default (null)", null)
}

@OptIn(PublicPreviewAPI::class)
enum class OnDeviceModelOptionSelection(val label: String, val option: OnDeviceModelOption?) {
    DEFAULT("Default (null)", null),
    PREVIEW("PREVIEW", OnDeviceModelOption.PREVIEW),
    PREVIEW_FAST("PREVIEW_FAST", OnDeviceModelOption.PREVIEW_FAST),
    STABLE("STABLE", OnDeviceModelOption.STABLE)
}

enum class GenerationMethod(val label: String) {
    UNARY("generateContent"),
    STREAMING("generateContentStream")
}

@OptIn(PublicPreviewAPI::class)
enum class InferenceModeOption(val label: String, val mode: InferenceMode) {
    ONLY_ON_DEVICE("ONLY_ON_DEVICE", InferenceMode.ONLY_ON_DEVICE),
    PREFER_ON_DEVICE("PREFER_ON_DEVICE", InferenceMode.PREFER_ON_DEVICE),
    PREFER_IN_CLOUD("PREFER_IN_CLOUD", InferenceMode.PREFER_IN_CLOUD),
    ONLY_IN_CLOUD("ONLY_IN_CLOUD", InferenceMode.ONLY_IN_CLOUD)
}

@OptIn(PublicPreviewAPI::class)
class ThinkingModeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ThinkingModeUiState())
    val uiState: StateFlow<ThinkingModeUiState> = _uiState

    private var testJob: Job? = null
    private var statusJob: Job? = null

    init {
        checkAndDownloadModel()
    }

    fun checkAndDownloadModel(modelOption: OnDeviceModelOption? = null) {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            try {
                val statusCheckModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    onDeviceConfig = OnDeviceConfig(
                        mode = InferenceMode.PREFER_ON_DEVICE,
                        modelOption = modelOption
                    )
                )
                val status = statusCheckModel.onDeviceExtension?.checkStatus()
                val statusText = when (status) {
                    OnDeviceModelStatus.AVAILABLE -> "Model available"
                    OnDeviceModelStatus.DOWNLOADABLE -> "Model downloadable"
                    OnDeviceModelStatus.DOWNLOADING -> "Model downloading..."
                    else -> "On-device model unavailable"
                }
                _uiState.update { it.copy(modelStatus = statusText) }

                if (status == OnDeviceModelStatus.DOWNLOADABLE) {
                    statusCheckModel.onDeviceExtension?.download()?.collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                _uiState.update { it.copy(modelStatus = "Downloading model...") }
                            }
                            is DownloadStatus.DownloadInProgress -> {
                                val progress = downloadStatus.totalBytesDownloaded
                                _uiState.update { it.copy(modelStatus = "Downloading: $progress bytes") }
                            }
                            is DownloadStatus.DownloadCompleted -> {
                                _uiState.update { it.copy(modelStatus = "Model available") }
                            }
                            is DownloadStatus.DownloadFailed -> {
                                _uiState.update {
                                    it.copy(
                                        modelStatus = "Download failed",
                                        errorMessage = "Model download failed"
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        modelStatus = "Error checking status",
                        errorMessage = e.localizedMessage ?: e.toString()
                    )
                }
            }
        }
    }

    fun runTest(
        promptText: String,
        thinkingOption: ThinkingOption,
        modelOptionSelection: OnDeviceModelOptionSelection,
        mode: InferenceMode,
        method: GenerationMethod
    ) {
        testJob?.cancel()
        testJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    thoughtSummary = null,
                    responseText = null,
                    inferenceSource = null,
                    logOutput = "Running inference..."
                )
            }

            Log.i(TAG, "==========================================================")
            Log.i(TAG, ">>> [THINKING MODE TEST - INPUT] >>>")
            Log.i(TAG, "enableThinking:     ${thinkingOption.enableThinking}")
            Log.i(TAG, "modelOption:        ${modelOptionSelection.label}")
            Log.i(TAG, "Inference Mode:     $mode")
            Log.i(TAG, "Generation Method:  ${method.label}")
            Log.i(TAG, "Prompt:             \"$promptText\"")
            Log.i(TAG, "==========================================================")

            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    generationConfig = generationConfig {
                        if (thinkingOption.enableThinking != null) {
                            thinkingConfig = thinkingConfig {
                                includeThoughts = thinkingOption.enableThinking
                            }
                        }
                    },
                    onDeviceConfig = OnDeviceConfig(
                        mode = mode,
                        modelOption = modelOptionSelection.option,
                        enableThinking = thinkingOption.enableThinking
                    )
                )

                var fullThought = ""
                var fullText = ""
                var source: InferenceSource? = null
                var totalChunks = 0
                var thoughtChunks = 0
                var textChunks = 0

                when (method) {
                    GenerationMethod.UNARY -> {
                        val response = model.generateContent(promptText)
                        fullThought = response.thoughtSummary.orEmpty()
                        fullText = response.text.orEmpty()
                        source = response.inferenceSource
                    }
                    GenerationMethod.STREAMING -> {
                        model.generateContentStream(promptText).collect { chunk ->
                            totalChunks++
                            val chunkThought = chunk.thoughtSummary
                            val chunkText = chunk.text
                            if (!chunkThought.isNullOrEmpty()) {
                                thoughtChunks++
                                fullThought += chunkThought
                            }
                            if (!chunkText.isNullOrEmpty()) {
                                textChunks++
                                fullText += chunkText
                            }
                            source = chunk.inferenceSource
                            _uiState.update {
                                it.copy(
                                    thoughtSummary = fullThought.ifEmpty { null },
                                    responseText = fullText.ifEmpty { null },
                                    inferenceSource = formatInferenceSource(source)
                                )
                            }
                        }
                    }
                }

                val sourceName = formatInferenceSource(source)
                Log.i(TAG, "<<< [THINKING MODE TEST - SUCCESS] <<<")
                Log.i(TAG, "Inference Source:   $sourceName")
                Log.i(TAG, "Thought Summary:    \"$fullThought\"")
                Log.i(TAG, "Response Text:      \"$fullText\"")

                val streamStats = if (method == GenerationMethod.STREAMING) {
                    "\nStream Chunks: total=$totalChunks, thoughtChunks=$thoughtChunks, textChunks=$textChunks"
                } else {
                    ""
                }

                val summaryLog = """
                    === INPUT ===
                    enableThinking: ${thinkingOption.enableThinking}
                    modelOption: ${modelOptionSelection.label}
                    Mode: $mode
                    Method: ${method.label}
                    Prompt: "$promptText"

                    === OUTPUT (SUCCESS) ===
                    Inference Source: $sourceName$streamStats
                    Thought Summary (${fullThought.length} chars):
                    ${fullThought.ifEmpty { "<none>" }}

                    Response (${fullText.length} chars):
                    $fullText
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        thoughtSummary = fullThought.ifEmpty { null },
                        responseText = fullText,
                        inferenceSource = sourceName,
                        logOutput = summaryLog
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val causeName = e.cause?.let { " (cause: ${it::class.java.simpleName})" }.orEmpty()
                Log.e(TAG, "<<< [THINKING MODE TEST - FAILURE] <<<", e)

                val errorLog = """
                    === INPUT ===
                    enableThinking: ${thinkingOption.enableThinking}
                    modelOption: ${modelOptionSelection.label}
                    Mode: $mode
                    Method: ${method.label}

                    === OUTPUT (FAILURE) ===
                    Exception: ${e::class.java.simpleName}$causeName
                    Message: ${e.localizedMessage ?: e.toString()}
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "${e::class.java.simpleName}$causeName: ${e.localizedMessage ?: e.toString()}",
                        logOutput = errorLog
                    )
                }
            }
        }
    }

    private fun formatInferenceSource(source: InferenceSource?): String =
        when (source) {
            InferenceSource.ON_DEVICE -> "ON_DEVICE"
            InferenceSource.IN_CLOUD -> "IN_CLOUD"
            null -> "UNKNOWN"
            else -> source.toString()
        }

    companion object {
        private const val TAG = "ThinkingModeDemo"
    }
}
