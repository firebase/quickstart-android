package com.google.firebase.quickstart.ai.ui

import android.graphics.Bitmap
import com.google.firebase.quickstart.ai.feature.hybrid.InferenceModeOption

data class HybridImagePatternUiState(
    val firstImage: Bitmap? = null,
    val secondImage: Bitmap? = null,
    val selectedMode: InferenceModeOption = InferenceModeOption.PREFER_ON_DEVICE,
    val isAnalyzing: Boolean = false,
    val modelStatus: String = "Checking model status...",
    val resultText: String? = null,
    val inferenceMode: String = "",
    val errorMessage: String? = null
)
