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
import com.google.firebase.ai.type.AutoFunctionDeclaration
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FirebaseAutoFunctionException
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository
import com.google.firebase.quickstart.ai.ui.FunctionCallInfo
import com.google.firebase.quickstart.ai.ui.FunctionCallingUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
object FunctionCallingRoute

enum class FunctionToolOption(val label: String) {
    MANUAL("Manual (FunctionDeclaration)"),
    AUTO("Automatic (AutoFunctionDeclaration)"),
    UNSUPPORTED("Unsupported Tool (Google Search)"),
    NONE("No Tools (Baseline)")
}

@OptIn(PublicPreviewAPI::class)
class FunctionCallingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FunctionCallingUiState())
    val uiState: StateFlow<FunctionCallingUiState> = _uiState

    private var testJob: Job? = null
    private var statusJob: Job? = null

    private val manualTools: List<Tool> = listOf(
        Tool.functionDeclarations(
            listOf(
                FunctionDeclaration(
                    name = "fetchWeather",
                    description = WEATHER_TOOL_DESCRIPTION,
                    parameters = mapOf(
                        "city" to Schema.string("The US city of the location."),
                        "state" to Schema.string("The US state of the location."),
                        "date" to Schema.string(
                            "The date for which to get the weather. Date must be in the format: YYYY-MM-DD."
                        )
                    )
                )
            )
        )
    )

    private val autoTools: List<Tool> = listOf(
        Tool.functionDeclarations(
            autoFunctionDeclarations = listOf(
                AutoFunctionDeclaration.create(
                    functionName = "fetchWeather",
                    description = WEATHER_TOOL_DESCRIPTION,
                    inputSchema = JsonSchema.obj(
                        properties = mapOf(
                            "city" to JsonSchema.string("The US city of the location."),
                            "state" to JsonSchema.string("The US state of the location."),
                            "date" to JsonSchema.string(
                                "The date for which to get the weather. Date must be in the format: YYYY-MM-DD."
                            )
                        )
                    ),
                    functionReference = ::fetchWeatherAuto
                )
            )
        )
    )

    private val unsupportedTools: List<Tool> = listOf(Tool.googleSearch())

    init {
        checkAndDownloadModel()
    }

    fun checkAndDownloadModel(modelOption: OnDeviceModelOption? = null) {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            try {
                val statusCheckModel = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = true
                ).generativeModel(
                    modelName = MODEL_NAME,
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

    private fun Map<String, JsonElement>.stringArg(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it != "null" }

    private suspend fun fetchWeatherAuto(input: JsonObject): FunctionResponsePart {
        val city = input.stringArg("city")
        val state = input.stringArg("state")
        val date = input.stringArg("date")

        return if (city == null || state == null || date == null) {
            throw FirebaseAutoFunctionException(MISSING_PARAMS_MESSAGE)
        } else {
            val response = WeatherRepository.fetchWeather(city, state, date)
            FunctionResponsePart("fetchWeather", response)
        }
    }

    private suspend fun executeManualWeatherCall(call: FunctionCallPart): JsonObject {
        val city = call.args.stringArg("city")
        val state = call.args.stringArg("state")
        val date = call.args.stringArg("date")

        return if (city == null || state == null || date == null) {
            JsonObject(mapOf("error" to JsonPrimitive(MISSING_PARAMS_MESSAGE)))
        } else {
            WeatherRepository.fetchWeather(city, state, date)
        }
    }

    private fun extractAutoHistoryDetails(
        history: List<Content>,
        functionCallInfos: MutableList<FunctionCallInfo>
    ): String? {
        var lastExecutionResult: String? = null
        for (entry in history) {
            for (part in entry.parts) {
                when (part) {
                    is FunctionCallPart -> {
                        functionCallInfos.add(
                            FunctionCallInfo(
                                name = part.name,
                                args = part.args.toString(),
                                id = part.id
                            )
                        )
                    }
                    is FunctionResponsePart -> {
                        lastExecutionResult = part.response.toString()
                    }
                }
            }
        }
        return lastExecutionResult
    }

    fun runTest(
        promptText: String,
        toolOption: FunctionToolOption,
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
                    functionCalls = emptyList(),
                    functionExecutionResult = null,
                    responseText = null,
                    inferenceSource = null,
                    logOutput = "Running inference..."
                )
            }

            Log.i(TAG, "==========================================================")
            Log.i(TAG, ">>> [FUNCTION CALLING TEST - INPUT] >>>")
            Log.i(TAG, "toolOption:         ${toolOption.label}")
            Log.i(TAG, "modelOption:        ${modelOptionSelection.label}")
            Log.i(TAG, "Inference Mode:     $mode")
            Log.i(TAG, "Generation Method:  ${method.label}")
            Log.i(TAG, "Prompt:             \"$promptText\"")
            Log.i(TAG, "==========================================================")

            try {
                val tools: List<Tool>? = when (toolOption) {
                    FunctionToolOption.MANUAL -> manualTools
                    FunctionToolOption.AUTO -> autoTools
                    FunctionToolOption.UNSUPPORTED -> unsupportedTools
                    FunctionToolOption.NONE -> null
                }

                val model = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = true
                ).generativeModel(
                    modelName = MODEL_NAME,
                    tools = tools,
                    onDeviceConfig = OnDeviceConfig(
                        mode = mode,
                        modelOption = modelOptionSelection.option,
                        tools = tools
                    )
                )

                val functionCallInfos = mutableListOf<FunctionCallInfo>()
                var fullText = ""
                var executionResult: String? = null
                var source: InferenceSource? = null

                when (method) {
                    GenerationMethod.UNARY -> {
                        if (toolOption == FunctionToolOption.AUTO) {
                            val chat = model.startChat()
                            val response = chat.sendMessage(promptText)
                            fullText = response.text.orEmpty()
                            source = response.inferenceSource
                            executionResult = extractAutoHistoryDetails(chat.history, functionCallInfos)
                        } else {
                            val response = model.generateContent(promptText)
                            val calls = response.functionCalls
                            fullText = response.text.orEmpty()
                            source = response.inferenceSource

                            for (call in calls) {
                                functionCallInfos.add(
                                    FunctionCallInfo(
                                        name = call.name,
                                        args = call.args.toString(),
                                        id = call.id
                                    )
                                )
                            }

                            if (toolOption == FunctionToolOption.MANUAL && calls.isNotEmpty()) {
                                val firstCall = calls.first()
                                val weatherJson = executeManualWeatherCall(firstCall)
                                executionResult = weatherJson.toString()

                                val followUp = model.generateContent(
                                    content(role = "user") { text(promptText) },
                                    content(role = "model") { part(firstCall) },
                                    content(role = "user") {
                                        part(FunctionResponsePart(firstCall.name, weatherJson, firstCall.id))
                                    }
                                )
                                fullText = followUp.text.orEmpty()
                                source = followUp.inferenceSource
                            }
                        }
                    }

                    GenerationMethod.STREAMING -> {
                        if (toolOption == FunctionToolOption.AUTO) {
                            val chat = model.startChat()
                            chat.sendMessageStream(promptText).collect { chunk ->
                                val chunkText = chunk.text
                                if (!chunkText.isNullOrEmpty()) {
                                    fullText += chunkText
                                }
                                source = chunk.inferenceSource
                                _uiState.update {
                                    it.copy(
                                        responseText = fullText.ifEmpty { null },
                                        inferenceSource = formatInferenceSource(source)
                                    )
                                }
                            }
                            executionResult = extractAutoHistoryDetails(chat.history, functionCallInfos)
                        } else {
                            val detectedCalls = mutableListOf<FunctionCallPart>()
                            model.generateContentStream(promptText).collect { chunk ->
                                val chunkText = chunk.text
                                if (!chunkText.isNullOrEmpty()) {
                                    fullText += chunkText
                                }
                                for (call in chunk.functionCalls) {
                                    detectedCalls.add(call)
                                    functionCallInfos.add(
                                        FunctionCallInfo(
                                            name = call.name,
                                            args = call.args.toString(),
                                            id = call.id
                                        )
                                    )
                                }
                                source = chunk.inferenceSource
                                _uiState.update {
                                    it.copy(
                                        functionCalls = functionCallInfos.toList(),
                                        responseText = fullText.ifEmpty { null },
                                        inferenceSource = formatInferenceSource(source)
                                    )
                                }
                            }

                            if (toolOption == FunctionToolOption.MANUAL && detectedCalls.isNotEmpty()) {
                                val firstCall = detectedCalls.first()
                                val weatherJson = executeManualWeatherCall(firstCall)
                                executionResult = weatherJson.toString()
                                fullText = ""

                                model.generateContentStream(
                                    content(role = "user") { text(promptText) },
                                    content(role = "model") { part(firstCall) },
                                    content(role = "user") {
                                        part(FunctionResponsePart(firstCall.name, weatherJson, firstCall.id))
                                    }
                                ).collect { followUpChunk ->
                                    val chunkText = followUpChunk.text
                                    if (!chunkText.isNullOrEmpty()) {
                                        fullText += chunkText
                                    }
                                    source = followUpChunk.inferenceSource
                                    _uiState.update {
                                        it.copy(
                                            functionCalls = functionCallInfos.toList(),
                                            functionExecutionResult = executionResult,
                                            responseText = fullText.ifEmpty { null },
                                            inferenceSource = formatInferenceSource(source)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val sourceName = formatInferenceSource(source)
                Log.i(TAG, "<<< [FUNCTION CALLING TEST - SUCCESS] <<<")
                Log.i(TAG, "Inference Source:   $sourceName")
                Log.i(TAG, "Function Calls:     ${functionCallInfos.size}")
                Log.i(TAG, "Response Text:      \"$fullText\"")

                val callsSummary = if (functionCallInfos.isNotEmpty()) {
                    functionCallInfos.joinToString("\n") { "- ${it.name}(${it.args}) [id=${it.id}]" }
                } else {
                    "<none>"
                }

                val summaryLog = """
                    === INPUT ===
                    toolOption: ${toolOption.label}
                    modelOption: ${modelOptionSelection.label}
                    Mode: $mode
                    Method: ${method.label}
                    Prompt: "$promptText"

                    === OUTPUT (SUCCESS) ===
                    Inference Source: $sourceName
                    Function Calls (${functionCallInfos.size}):
                    $callsSummary

                    Execution Result:
                    ${executionResult ?: "<none>"}

                    Final Response (${fullText.length} chars):
                    $fullText
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        functionCalls = functionCallInfos,
                        functionExecutionResult = executionResult,
                        responseText = fullText,
                        inferenceSource = sourceName,
                        logOutput = summaryLog
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val causeName = e.cause?.let { " (cause: ${it::class.java.simpleName})" }.orEmpty()
                Log.e(TAG, "<<< [FUNCTION CALLING TEST - FAILURE] <<<", e)

                val errorLog = """
                    === INPUT ===
                    toolOption: ${toolOption.label}
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
        private const val TAG = "FunctionCallingDemo"
        private const val MODEL_NAME = "gemini-3.5-flash-lite"
        private const val WEATHER_TOOL_DESCRIPTION =
            "Get the weather conditions for a specific US city on a specific date."
        private const val MISSING_PARAMS_MESSAGE = "Unable to fetch weather - parameters were missing"
    }
}
