package com.google.firebase.quickstart.ai.ui

sealed interface SvgUiState {
    data object Idle : SvgUiState
    data object Loading : SvgUiState
    data class Success(val svgs: List<String> = emptyList()) : SvgUiState
    data class Error(val message: String) : SvgUiState
}
