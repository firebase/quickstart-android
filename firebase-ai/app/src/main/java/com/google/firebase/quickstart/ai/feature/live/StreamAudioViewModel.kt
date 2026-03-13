package com.google.firebase.quickstart.ai.feature.live

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository.Companion.fetchWeather
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
object StreamRealtimeAudioRoute

@OptIn(PublicPreviewAPI::class)
class StreamAudioViewModel : BidiViewModel() {
    init {
        val liveGenerationConfig = liveGenerationConfig {
            speechConfig = SpeechConfig(voice = Voice("CHARON"))
            responseModality = ResponseModality.AUDIO
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

    override fun handler(functionCall: FunctionCallPart): FunctionResponsePart {
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
}
