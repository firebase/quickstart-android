package com.google.firebase.quickstart.ai.feature.live

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.InlineData
import com.google.firebase.ai.type.InteractionStatus
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.liveAudioConversationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.io.ByteArrayOutputStream


@OptIn(PublicPreviewAPI::class)
abstract class BidiViewModel : ViewModel() {
    protected lateinit var liveSession: LiveSession

    private val _interactionStatus = MutableStateFlow<InteractionStatus?>(null)
    val interactionStatus: StateFlow<InteractionStatus?> = _interactionStatus.asStateFlow()

    private val _turnComplete = MutableStateFlow(false)
    val turnComplete: StateFlow<Boolean> = _turnComplete.asStateFlow()

    open fun handler(functionCall: FunctionCallPart): FunctionResponsePart {
        return FunctionResponsePart(functionCall.name, JsonObject(emptyMap()), functionCall.id)
    }

    // The permission check is handled by the view that calls this function.
    @SuppressLint("MissingPermission")
    suspend fun startConversation() {
        _turnComplete.value = false
        liveSession.startAudioConversation(
            liveAudioConversationConfig {
                functionCallHandler = ::handler
                interactionStatusHandler = { status ->
                    _interactionStatus.value = status
                }
                turnCompleteHandler = { isTurnComplete, status ->
                    _turnComplete.value = isTurnComplete
                    if (status != null) {
                        _interactionStatus.value = status
                    }
                }
            }
        )
    }

    fun endConversation() {
        liveSession.stopAudioConversation()
        _interactionStatus.value = null
        _turnComplete.value = false
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
