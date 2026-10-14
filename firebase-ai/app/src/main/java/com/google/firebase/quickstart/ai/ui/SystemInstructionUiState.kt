package com.google.firebase.quickstart.ai.ui

data class SystemInstructionUiState(
    val modelStatus: String = "Checking model status...",
    val isLoading: Boolean = false,
    val responseText: String? = null,
    val inferenceSource: String? = null,
    val logOutput: String = "Configure system instruction options above and tap Run.",
    val errorMessage: String? = null
)
