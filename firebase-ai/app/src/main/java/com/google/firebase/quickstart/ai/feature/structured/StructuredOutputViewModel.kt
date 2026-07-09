package com.google.firebase.quickstart.ai.feature.structured

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.quickstart.ai.ui.StructuredOutputUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
object StructuredOutputRoute

enum class GenerationWorkflow {
    GENERATE_OBJECT, // Typed KSP Schema
    GENERATE_CONTENT // Manual JsonSchema
}

@OptIn(PublicPreviewAPI::class)
class StructuredOutputViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StructuredOutputUiState())
    val uiState: StateFlow<StructuredOutputUiState> = _uiState

    private fun getModel(mode: InferenceMode) =
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.1-flash-lite",
            onDeviceConfig = OnDeviceConfig(mode = mode)
        )

    fun runStructuredInference(
        promptText: String,
        mode: InferenceMode,
        workflow: GenerationWorkflow
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    resultObject = null,
                    rawJson = null,
                    inferenceSource = null,
                    logOutput = "Starting inference..."
                )
            }

            val modeString = when (mode) {
                InferenceMode.PREFER_IN_CLOUD -> "PREFER_IN_CLOUD (Hybrid - Prefer Cloud)"
                InferenceMode.PREFER_ON_DEVICE -> "PREFER_ON_DEVICE (Hybrid Fallback)"
                InferenceMode.ONLY_ON_DEVICE -> "ONLY_ON_DEVICE (ML Kit)"
                InferenceMode.ONLY_IN_CLOUD -> "ONLY_IN_CLOUD (Vertex/Google AI)"
                else -> mode.toString()
            }

            val schemaDescription = if (workflow == GenerationWorkflow.GENERATE_OBJECT) {
                "MovieReview.firebaseAISchema() (KSP Companion Class)"
            } else {
                "JsonSchema.obj(MovieReview::class) (Manual Schema)"
            }

            Log.i(TAG, "==========================================================")
            Log.i(TAG, ">>> [MANUAL CHECK - INPUT] >>>")
            Log.i(TAG, "Workflow:        ${workflow.name}")
            Log.i(TAG, "Inference Mode:  $modeString")
            Log.i(TAG, "Schema Type:     $schemaDescription")
            Log.i(TAG, "Prompt Text:     \"$promptText\"")
            Log.i(TAG, "==========================================================")

            try {
                val model = getModel(mode)
                var parsedObject: MovieReview? = null
                var rawTextString: String? = null
                var actualSource: String? = null

                when (workflow) {
                    GenerationWorkflow.GENERATE_OBJECT -> {
                        Log.d(TAG, "Executing generateObject with KSP schema...")
                        val schema = MovieReview.firebaseAISchema()
                        val response = model.generateObject(schema, promptText)
                        
                        parsedObject = response.getObject()
                        rawTextString = response.response.text
                        actualSource = response.response.inferenceSource?.toString() ?: "UNKNOWN"
                    }

                    GenerationWorkflow.GENERATE_CONTENT -> {
                        Log.d(TAG, "Executing generateContent with manual schema...")
                        val manualSchema = JsonSchema.obj(
                            properties = mapOf(
                                "title" to JsonSchema.string(),
                                "summary" to JsonSchema.string(),
                                "rating" to JsonSchema.integer(),
                                "tags" to JsonSchema.array(items = JsonSchema.string())
                            ),
                            clazz = MovieReview::class
                        )

                        val customModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                            modelName = "gemini-3.1-flash-lite",
                            generationConfig = generationConfig {
                                responseMimeType = "application/json"
                                responseJsonSchema = manualSchema
                            },
                            onDeviceConfig = OnDeviceConfig(mode = mode)
                        )

                        val response = customModel.generateContent(promptText)
                        rawTextString = response.text
                        actualSource = response.inferenceSource?.toString() ?: "UNKNOWN"

                        if (rawTextString != null) {
                            val cleanJson = rawTextString
                                .replace("```json", "")
                                .replace("```", "")
                                .trim()
                            parsedObject = Json.decodeFromString<MovieReview>(cleanJson)
                        }
                    }
                }

                Log.i(TAG, "==========================================================")
                Log.i(TAG, "<<< [MANUAL CHECK - OUTPUT (SUCCESS)] <<<")
                Log.i(TAG, "Workflow:        ${workflow.name}")
                Log.i(TAG, "Actual Source:   $actualSource")
                Log.i(TAG, "Raw Text / JSON: \"${rawTextString ?: "(Empty - ML Kit returned in-memory typed object directly)"}\"")
                Log.i(TAG, "Parsed Object:   $parsedObject")
                Log.i(TAG, "==========================================================")

                val summaryLog = """
                    === INPUT ===
                    Schema: $schemaDescription
                    Prompt: "$promptText"
                    
                    === OUTPUT (SUCCESS) ===
                    Source: $actualSource
                    Parsed Object:
                    $parsedObject
                    
                    Raw Text/JSON: '${rawTextString ?: "(Empty - ML Kit typed object)"}'
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultObject = parsedObject,
                        rawJson = rawTextString,
                        inferenceSource = actualSource,
                        logOutput = summaryLog
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "==========================================================")
                Log.e(TAG, "<<< [MANUAL CHECK - OUTPUT (FAILURE)] <<<")
                Log.e(TAG, "Workflow:        ${workflow.name}")
                Log.e(TAG, "Inference Mode:  $modeString")
                Log.e(TAG, "Exception Type:  ${e::class.java.name}")
                Log.e(TAG, "Error Message:   ${e.localizedMessage ?: e.toString()}")
                Log.e(TAG, "==========================================================", e)

                val errorLog = """
                    === INPUT ===
                    Schema: $schemaDescription
                    Prompt: "$promptText"
                    
                    === OUTPUT (FAILURE) ===
                    Status: FAILED
                    Exception: ${e::class.java.simpleName}
                    Message: ${e.localizedMessage ?: e.toString()}
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: e.toString(),
                        logOutput = errorLog
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "StructuredOutputDemo"
    }
}
