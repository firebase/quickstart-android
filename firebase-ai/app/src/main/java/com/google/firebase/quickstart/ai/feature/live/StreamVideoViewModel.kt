package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Transcription
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream

@Serializable
object StreamRealtimeVideoRoute

@OptIn(PublicPreviewAPI::class)
class StreamVideoViewModel : ViewModel() {
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

        // Note that each backend supports a different set of models.
        // See our documentation for a breakdown of models by backend:
        // https://firebase.google.com/docs/ai-logic/live-api#supported-models
        val liveModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).liveModel(
            modelName = "gemini-2.5-flash-native-audio-preview-09-2025",
            generationConfig = liveGenerationConfig,
        )
        runBlocking { liveSession = liveModel.connect() }
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
    suspend fun startConversation() {
        liveSession.startAudioConversation(null, ::handleTranscription)
    }

    fun endConversation() {
        liveSession.stopAudioConversation()
    }

    fun clearTranscriptions() {
        _transcriptions.value = emptyList()
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
