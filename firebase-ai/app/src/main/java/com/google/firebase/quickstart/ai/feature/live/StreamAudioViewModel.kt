package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.Transcription
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository.Companion.fetchWeather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
object StreamRealtimeAudioRoute

enum class TranscriptionSpeaker {
    USER,
    MODEL
}

data class TranscriptionItem(
    val speaker: TranscriptionSpeaker,
    val text: String
)

@OptIn(PublicPreviewAPI::class)
class StreamAudioViewModel : ViewModel() {
    private var liveSession: LiveSession

    private val _transcriptions = MutableStateFlow<List<TranscriptionItem>>(emptyList())
    val transcriptions: StateFlow<List<TranscriptionItem>> = _transcriptions.asStateFlow()

    init {
        val liveGenerationConfig = liveGenerationConfig {
            speechConfig = SpeechConfig(voice = Voice("CHARON"))
            responseModality = ResponseModality.AUDIO
            inputAudioTranscription = AudioTranscriptionConfig()
            outputAudioTranscription = AudioTranscriptionConfig()
        }

        val liveModel =
            Firebase.ai(backend = GenerativeBackend.googleAI())
                .liveModel(
                    // Note that each backend supports a different set of models.
                    // See our documentation for a breakdown of models by backend:
                    // https://firebase.google.com/docs/ai-logic/live-api#supported-models
                    modelName = "gemini-2.5-flash-native-audio-preview-09-2025",
                    generationConfig = liveGenerationConfig,
                    tools = listOf(
                        Tool.functionDeclarations(
                            listOf(
                                FunctionDeclaration(
                                    "fetchWeather",
                                    "Get the weather conditions for a specific US city on a specific date.",
                                    mapOf(
                                        "city" to Schema.string("The US city of the location."),
                                        "state" to Schema.string("The US state of the location."),
                                        "date" to Schema.string(
                                            "The date for which to get the weather." +
                                                    " Date must be in the format: YYYY-MM-DD."
                                        ),
                                    ),
                                )
                            )
                        )
                    ),
                )
        runBlocking { liveSession = liveModel.connect() }
    }

    fun handleFunctionCall(functionCall: FunctionCallPart): FunctionResponsePart {
        val response: JsonObject
        if (functionCall.name == "fetchWeather") {
            val city = functionCall.args["city"]?.jsonPrimitive?.content
            val state = functionCall.args["state"]?.jsonPrimitive?.content
            val date = functionCall.args["date"]?.jsonPrimitive?.content
            runBlocking {
                response =
                    if (!city.isNullOrEmpty() and !state.isNullOrEmpty() and !date.isNullOrEmpty()) {
                        fetchWeather(city!!, state!!, date!!)
                    } else {
                        JsonObject(emptyMap())
                    }
            }
        } else {
            response = JsonObject(emptyMap())
        }
        return FunctionResponsePart(functionCall.name, response, functionCall.id)
    }

    private fun handleTranscription(input: Transcription?, output: Transcription?) {
        input?.text?.let { text ->
            if (text.isNotEmpty()) {
                _transcriptions.update { current ->
                    val last = current.lastOrNull()
                    if (last != null && last.speaker == TranscriptionSpeaker.USER) {
                        current.dropLast(1) + last.copy(text = last.text + text)
                    } else {
                        current + TranscriptionItem(speaker = TranscriptionSpeaker.USER, text = text)
                    }
                }
            }
        }
        output?.text?.let { text ->
            if (text.isNotEmpty()) {
                _transcriptions.update { current ->
                    val last = current.lastOrNull()
                    if (last != null && last.speaker == TranscriptionSpeaker.MODEL) {
                        current.dropLast(1) + last.copy(text = last.text + text)
                    } else {
                        current + TranscriptionItem(speaker = TranscriptionSpeaker.MODEL, text = text)
                    }
                }
            }
        }
    }

    // The permission check is handled by the view that calls this function.
    @SuppressLint("MissingPermission")
    fun startConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            liveSession.startAudioConversation(::handleFunctionCall, ::handleTranscription)
        }
    }

    fun endConversation() {
        liveSession.stopAudioConversation()
    }

    fun clearTranscriptions() {
        _transcriptions.value = emptyList()
    }
}
