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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.io.ByteArrayOutputStream


@OptIn(PublicPreviewAPI::class)
abstract class BidiViewModel : ViewModel() {
    protected lateinit var liveSession: LiveSession

    open fun handler(functionCall: FunctionCallPart): FunctionResponsePart {
        return FunctionResponsePart(functionCall.name, JsonObject(emptyMap()), functionCall.id)
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
