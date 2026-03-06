package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.TemplateImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.quickstart.ai.ui.ImagenUiState
import kotlinx.serialization.Serializable

@Serializable
object ImagenTemplateRoute

@OptIn(PublicPreviewAPI::class)
class ImagenTemplateViewModel : ImagenViewModel() {
    override val initialPrompt: String = "List of things that should be in the image"
    override val includeAttach: Boolean = false
    override val selectionOptions: List<String> = emptyList()
    override val allowEmptyPrompt: Boolean = false
    override val additionalImage: Bitmap? = null
    override val imageLabels: List<String> = emptyList()

    private val templateImagenModel: TemplateImagenModel = Firebase.ai.templateImagenModel()

    override suspend fun performGeneration(
        inputText: String,
        currentState: ImagenUiState.Success
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return try {
            templateImagenModel.generateImages("imagen-basic", mapOf("prompt" to inputText))
        } catch (e: Exception) {
            if (e.localizedMessage?.contains("not found") == true) {
                throw Exception("Template was not found, please verify that your project contains a template named \"imagen-basic\".")
            } else {
                throw e
            }
        }
    }
}
