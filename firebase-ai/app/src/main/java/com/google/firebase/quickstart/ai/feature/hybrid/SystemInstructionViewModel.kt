package com.google.firebase.quickstart.ai.feature.hybrid

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
import com.google.firebase.quickstart.ai.MainActivity
import com.google.firebase.quickstart.ai.ui.SystemInstructionUiState
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
class SystemInstructionViewModel : ViewModel() {
    val uiState: StateFlow<SystemInstructionUiState>
        field = MutableStateFlow(SystemInstructionUiState())

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
                uiState.update { it.copy(modelStatus = statusText) }

                if (status == OnDeviceModelStatus.DOWNLOADABLE) {
                    statusCheckModel.onDeviceExtension?.download()?.collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.DownloadStarted -> {
                                uiState.update { it.copy(modelStatus = "Downloading model...") }
                            }
                            is DownloadStatus.DownloadInProgress -> {
                                val progress = downloadStatus.totalBytesDownloaded
                                uiState.update { it.copy(modelStatus = "Downloading: $progress bytes") }
                            }
                            is DownloadStatus.DownloadCompleted -> {
                                uiState.update { it.copy(modelStatus = "Model available") }
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
                uiState.update {
                    it.copy(
                        modelStatus = "Error checking status",
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun buildSystemInstruction(
        case: SystemInstructionCase,
        primaryInstruction: String,
        secondaryInstruction: String
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
                image(MainActivity.catImage)
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
        method: GenerationMethod
    ) {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    responseText = null,
                    inferenceSource = null,
                    logOutput = "Running inference..."
                )
            }

            val systemInstruction = buildSystemInstruction(case, primaryInstruction, secondaryInstruction)
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
                            if (chunk.inferenceSource != null) {
                                source = chunk.inferenceSource
                            }
                            uiState.update {
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

                uiState.update {
                    it.copy(
                        isLoading = false,
                        responseText = fullText,
                        inferenceSource = sourceName,
                        logOutput = summaryLog
                    )
                }
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

                uiState.update {
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
