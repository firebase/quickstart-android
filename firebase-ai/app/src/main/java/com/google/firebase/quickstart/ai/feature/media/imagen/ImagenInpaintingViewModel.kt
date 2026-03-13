package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenBackgroundMask
import com.google.firebase.ai.type.ImagenEditMode
import com.google.firebase.ai.type.ImagenEditingConfig
import com.google.firebase.ai.type.ImagenForegroundMask
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenRawImage
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.ui.ImagenUiState
import kotlinx.serialization.Serializable

@Serializable
object ImagenInpaintingRoute

@OptIn(PublicPreviewAPI::class)
class ImagenInpaintingViewModel : ImagenViewModel() {
    override val initialPrompt: String = "A sunny beach"
    override val includeAttach: Boolean = true
    override val selectionOptions: List<String> = listOf("Mask", "Background", "Foreground")
    override val allowEmptyPrompt: Boolean = true
    override val additionalImage: Bitmap? = null
    override val imageLabels: List<String> = emptyList()

    private val imagenModel: ImagenModel

    init {
        val config = imagenGenerationConfig {
            numberOfImages = 4
            imageFormat = ImagenImageFormat.png()
        }
        val settings = ImagenSafetySettings(
            safetyFilterLevel = ImagenSafetyFilterLevel.BLOCK_LOW_AND_ABOVE,
            personFilterLevel = ImagenPersonFilterLevel.BLOCK_ALL
        )
        imagenModel = Firebase.ai(
            backend = GenerativeBackend.vertexAI()
        ).imagenModel(
            modelName = "imagen-3.0-capability-001",
            generationConfig = config,
            safetySettings = settings
        )
    }

    override suspend fun performGeneration(
        inputText: String,
        currentState: ImagenUiState.Success
    ): ImagenGenerationResponse<ImagenInlineImage> {
        val bitmap = currentState.attachedImage!!
        val mask = when (currentState.selectedOption) {
            "Foreground" -> ImagenForegroundMask()
            else -> ImagenBackgroundMask()
        }
        return imagenModel.editImage(
            listOfNotNull(ImagenRawImage(bitmap.toImagenInlineImage()), mask),
            inputText,
            ImagenEditingConfig(ImagenEditMode.INPAINT_INSERTION)
        )
    }
}
