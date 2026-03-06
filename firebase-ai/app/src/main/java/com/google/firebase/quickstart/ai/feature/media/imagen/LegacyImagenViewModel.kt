package com.google.firebase.quickstart.ai.feature.media.imagen

import kotlinx.serialization.Serializable

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import com.google.firebase.ai.type.asTextOrNull

@Serializable
class ImagenRoute(val sampleId: String)

class LegacyImagenViewModel(
    savedStateHandle: SavedStateHandle
) : ImagenViewModel(
    initialPrompt = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty(),
    modelName = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.modelName ?: "imagen-4.0-generate-001",
    backend = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.backend,
    includeAttach = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.includeAttach,
    selectionOptions = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.selectionOptions,
    allowEmptyPrompt = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.allowEmptyPrompt,
    additionalImage = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.additionalImage,
    imageLabels = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.imageLabels,
    editingMode = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.editingMode,
    templateId = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.templateId,
    templateKey = FIREBASE_AI_SAMPLES.first { it.id == savedStateHandle.toRoute<ImagenRoute>().sampleId }.templateKey
)
