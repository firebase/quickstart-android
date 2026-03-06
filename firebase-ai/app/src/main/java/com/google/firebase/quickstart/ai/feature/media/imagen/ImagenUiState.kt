package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap

sealed interface ImagenUiState {
    data object Idle : ImagenUiState
    data object Loading : ImagenUiState
    data class Success(
        val images: List<Bitmap> = emptyList(),
        val attachedImage: Bitmap? = null,
        val selectedOption: String? = null
    ) : ImagenUiState
    data class Error(val message: String) : ImagenUiState
}
