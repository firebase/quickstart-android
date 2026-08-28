package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Transcription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.io.ByteArrayOutputStream

enum class TranscriptionSpeaker {
    USER,
    MODEL
}

data class TranscriptionItem(
    val speaker: TranscriptionSpeaker,
    val text: String
)

@OptIn(PublicPreviewAPI::class)
abstract class BidiViewModel : ViewModel() {
    protected lateinit var liveSession: LiveSession

    private val _transcriptions = MutableStateFlow<List<TranscriptionItem>>(emptyList())
    val transcriptions: StateFlow<List<TranscriptionItem>> = _transcriptions.asStateFlow()

    open fun handler(functionCall: FunctionCallPart): FunctionResponsePart {
        return FunctionResponsePart(functionCall.name, JsonObject(emptyMap()), functionCall.id)
    }

    open fun handleTranscription(input: Transcription?, output: Transcription?) {
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
        liveSession.startAudioConversation(::handler, ::handleTranscription)
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
