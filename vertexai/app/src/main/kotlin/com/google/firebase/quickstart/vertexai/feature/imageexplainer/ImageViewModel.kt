package com.google.firebase.quickstart.vertexai.feature.imageexplainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ImageViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<ImageUiState> =
        MutableStateFlow(ImageUiState.Success("Answer will be here"))
    val uiState: StateFlow<ImageUiState> =
        _uiState.asStateFlow()

    private val generativeModel = Firebase.vertexAI
        .generativeModel("gemini-1.5-flash-preview-0514")

    /**
     * Asks Gemini to explain the specified image
     */
    fun explainImage(
        imageRef: StorageReference,
        textPrompt: String
    ) {
        viewModelScope.launch {
            _uiState.value = ImageUiState.Loading

            try {
                // Get the image metadata to obtain the gs:// URL and mimeType
                val metadata = imageRef.metadata.await()
                val mimeType = metadata.contentType!!
                val gsURL = "gs://${metadata.bucket}/${metadata.path}"

                // Send the image to Gemini
                val result = generativeModel.generateContent(content {
                    fileData(gsURL, mimeType)
                    text(textPrompt)
                })
                _uiState.value = ImageUiState.Success(result.text!!)
            } catch (e: Exception) {
                _uiState.value = ImageUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}