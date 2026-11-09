package com.google.firebase.quickstart.ai.feature.hybrid

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceModelStatus
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.SystemInstructionUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SystemInstructionRoute

enum class SystemInstructionCase(val label: String) {
    SINGLE_TEXT("Single TextPart"),
    MULTIPLE_TEXT("Multiple TextParts"),
    NON_TEXT_PART("Non-Text Part (Image)"),
    NONE("No System Instruction")
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
class SystemInstructionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SystemInstructionUiState())
    val uiState: StateFlow<SystemInstructionUiState> = _uiState

    private var testJob: Job? = null

    private val statusCheckModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-3.5-flash-lite",
        onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
    )

    init {
        checkAndDownloadModel()
    }

    private fun checkAndDownloadModel() {
        viewModelScope.launch {
            try {
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

    private fun buildSystemInstruction(
        case: SystemInstructionCase,
        primaryInstruction: String,
        secondaryInstruction: String,
        testImage: Bitmap?
    ): Content? {
        return when (case) {
            SystemInstructionCase.SINGLE_TEXT -> content {
                text(primaryInstruction)
            }
            SystemInstructionCase.MULTIPLE_TEXT -> content {
                text(primaryInstruction)
                text(secondaryInstruction)
            }
            SystemInstructionCase.NON_TEXT_PART -> content {
                text(primaryInstruction)
                if (testImage != null) {
                    image(testImage)
                }
            }
            SystemInstructionCase.NONE -> null
        }
    }

    fun runTest(
        promptText: String,
        primaryInstruction: String,
        secondaryInstruction: String,
        case: SystemInstructionCase,
        mode: InferenceMode,
        method: GenerationMethod,
        testImage: Bitmap? = null
    ) {
        testJob?.cancel()
        testJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    responseText = null,
                    inferenceSource = null,
                    logOutput = "Running inference..."
                )
            }

            val systemInstruction =
                buildSystemInstruction(case, primaryInstruction, secondaryInstruction, testImage)
            val partsSummary = systemInstruction?.parts?.joinToString(", ") { part ->
                part::class.java.simpleName
            } ?: "null"

            Log.i(TAG, "==========================================================")
            Log.i(TAG, ">>> [SYSTEM INSTRUCTION TEST - INPUT] >>>")
            Log.i(TAG, "Case:               ${case.name} ($partsSummary)")
            Log.i(TAG, "Inference Mode:     $mode")
            Log.i(TAG, "Generation Method:  ${method.label}")
            Log.i(TAG, "Prompt:             \"$promptText\"")
            Log.i(TAG, "==========================================================")

            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    onDeviceConfig = OnDeviceConfig(mode = mode),
                    systemInstruction = systemInstruction
                )

                var fullText = ""
                var source: InferenceSource? = null

                when (method) {
                    GenerationMethod.UNARY -> {
                        val response = model.generateContent(promptText)
                        fullText = response.text.orEmpty()
                        source = response.inferenceSource
                    }
                    GenerationMethod.STREAMING -> {
                        model.generateContentStream(promptText).collect { chunk ->
                            fullText += chunk.text.orEmpty()
                            source = chunk.inferenceSource
                            _uiState.update {
                                it.copy(
                                    responseText = fullText,
                                    inferenceSource = formatInferenceSource(source)
                                )
                            }
                        }
                    }
                }

                val sourceName = formatInferenceSource(source)
                Log.i(TAG, "<<< [SYSTEM INSTRUCTION TEST - SUCCESS] <<<")
                Log.i(TAG, "Inference Source:   $sourceName")
                Log.i(TAG, "Response Text:      \"$fullText\"")

                val summaryLog = """
                    === INPUT ===
                    Case: ${case.label} (parts: $partsSummary)
                    Mode: $mode
                    Method: ${method.label}
                    Prompt: "$promptText"

                    === OUTPUT (SUCCESS) ===
                    Inference Source: $sourceName
                    Response:
                    $fullText
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        responseText = fullText,
                        inferenceSource = sourceName,
                        logOutput = summaryLog
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val causeName = e.cause?.let { " (cause: ${it::class.java.simpleName})" }.orEmpty()
                Log.e(TAG, "<<< [SYSTEM INSTRUCTION TEST - FAILURE] <<<", e)

                val errorLog = """
                    === INPUT ===
                    Case: ${case.label} (parts: $partsSummary)
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
        private const val TAG = "SystemInstructionDemo"
    }
}
