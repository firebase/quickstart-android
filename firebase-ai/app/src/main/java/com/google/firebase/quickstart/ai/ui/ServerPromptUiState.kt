package com.google.firebase.quickstart.ai.ui

sealed interface ServerPromptUiState {
    data object Idle : ServerPromptUiState
    data object Loading : ServerPromptUiState
    data class Success(val generatedText: String? = null) : ServerPromptUiState
    data class Error(val message: String) : ServerPromptUiState
}
