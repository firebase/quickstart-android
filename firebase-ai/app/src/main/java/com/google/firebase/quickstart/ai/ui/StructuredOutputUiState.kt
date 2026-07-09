package com.google.firebase.quickstart.ai.ui

import com.google.firebase.quickstart.ai.feature.structured.MovieReview

data class StructuredOutputUiState(
    val isLoading: Boolean = false,
    val resultObject: MovieReview? = null,
    val rawJson: String? = null,
    val inferenceSource: String? = null,
    val logOutput: String = "Select workflow and inference mode, then tap Generate to test.",
    val errorMessage: String? = null
)
