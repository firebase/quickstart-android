package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.quickstart.ai.ui.ImagenUiState
import kotlinx.serialization.Serializable

@Serializable
object ImagenGenerationRoute

@OptIn(PublicPreviewAPI::class)
class ImagenGenerationViewModel : ImagenViewModel() {
    override val initialPrompt: String = ""
    override val includeAttach: Boolean = false
    override val selectionOptions: List<String> = emptyList()
    override val allowEmptyPrompt: Boolean = false
    override val additionalImage: Bitmap? = null
    override val imageLabels: List<String> = emptyList()

    private val imagenModel: ImagenModel

    init {
        imagenModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).imagenModel(
            modelName = "imagen-4.0-generate-001",
            generationConfig = imagenGenerationConfig {
                numberOfImages = 4
                imageFormat = ImagenImageFormat.png()
            },
            safetySettings = ImagenSafetySettings(
                safetyFilterLevel = ImagenSafetyFilterLevel.BLOCK_LOW_AND_ABOVE,
                personFilterLevel = ImagenPersonFilterLevel.BLOCK_ALL
            )
        )
    }

    override suspend fun performGeneration(
        inputText: String,
        currentState: ImagenUiState.Success
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return imagenModel.generateImages(inputText)
    }
}
