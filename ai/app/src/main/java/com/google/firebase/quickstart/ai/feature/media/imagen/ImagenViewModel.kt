package com.google.firebase.quickstart.ai.feature.media.imagen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.google.firebase.quickstart.ai.feature.text.ChatRoute
import com.google.firebase.quickstart.ai.ui.navigation.FIREBASE_AI_SAMPLES

class ImagenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ImagenRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }

}
