package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.app
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository.Companion.fetchWeather
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(PublicPreviewAPI::class)
class BidiViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<StreamRealtimeRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }

    // Firebase AI Logic
    private var liveSession: LiveSession

    init {
        val liveGenerationConfig = liveGenerationConfig {
            speechConfig = SpeechConfig(voice = Voice("CHARON"))
            // Change this to ContentModality.TEXT if you want text output.
            responseModality = ResponseModality.AUDIO
        }

        @OptIn(PublicPreviewAPI::class)
        val liveModel =
            FirebaseAI.getInstance(Firebase.app, sample.backend)
                .liveModel(
                    // If you are using Vertex AI, change the model name to
                    // "gemini-live-2.5-flash-preview-native-audio-09-2025"
                    modelName = sample.modelName ?: "gemini-2.5-flash-native-audio-preview-09-2025",
                    generationConfig = liveGenerationConfig,
                    tools = sample.tools,
                )
        runBlocking { liveSession = liveModel.connect() }
    }

    fun handler(fetchWeatherCall: FunctionCallPart): FunctionResponsePart {
        val response: JsonObject
        fetchWeatherCall.let {
            val city = it.args["city"]?.jsonPrimitive?.content
            val state = it.args["state"]?.jsonPrimitive?.content
            val date = it.args["date"]?.jsonPrimitive?.content
            runBlocking {
                response =
                    if (!city.isNullOrEmpty() and !state.isNullOrEmpty() and !date.isNullOrEmpty()) {
                        fetchWeather(city!!, state!!, date!!)
                    } else {
                        JsonObject(emptyMap())
                    }
            }
        }
        return FunctionResponsePart("fetchWeather", response, fetchWeatherCall.id)
    }

    // The permission check is handled by the view that calls this function.
    @SuppressLint("MissingPermission")
    suspend fun startConversation() {
        liveSession.startAudioConversation(::handler)
    }

    fun endConversation() {
        liveSession.stopAudioConversation()
    }

    fun sendVideoFrame(frame: Bitmap) {
        viewModelScope.launch {
            // Directly compress the Bitmap to a ByteArray
            val byteArrayOutputStream = ByteArrayOutputStream()
            frame.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val jpegBytes = byteArrayOutputStream.toByteArray()

            liveSession.sendVideoRealtime(InlineData(jpegBytes, "image/jpeg"))
        }
    }
}
