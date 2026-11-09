package com.google.firebase.quickstart.ai.ui

data class ThinkingModeUiState(
    val modelStatus: String = "Checking model status...",
    val isLoading: Boolean = false,
    val thoughtSummary: String? = null,
    val responseText: String? = null,
    val inferenceSource: String? = null,
    val logOutput: String = "Configure thinking mode options above and tap Run.",
    val errorMessage: String? = null
)
