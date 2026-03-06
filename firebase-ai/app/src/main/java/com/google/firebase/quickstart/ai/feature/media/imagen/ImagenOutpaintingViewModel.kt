package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Dimensions
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenEditMode
import com.google.firebase.ai.type.ImagenEditingConfig
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenImagePlacement
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenMaskReference
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenRawMask
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.toImagenInlineImage
import kotlinx.serialization.Serializable

@Serializable
object ImagenOutpaintingRoute

@OptIn(PublicPreviewAPI::class)
class ImagenOutpaintingViewModel : ImagenViewModel() {
    override val initialPrompt: String = ""
    override val includeAttach: Boolean = true
    override val selectionOptions: List<String> = listOf("Image Alignment", "Center", "Top", "Bottom", "Left", "Right")
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
        val position = when (currentState.selectedOption) {
            "Top" -> ImagenImagePlacement.TOP_CENTER
            "Bottom" -> ImagenImagePlacement.BOTTOM_CENTER
            "Left" -> ImagenImagePlacement.LEFT_CENTER
            "Right" -> ImagenImagePlacement.RIGHT_CENTER
            else -> ImagenImagePlacement.CENTER
        }
        val dimensions = Dimensions(bitmap.width * 2, bitmap.height * 2)
        val (sourceImage, mask) = ImagenMaskReference.generateMaskAndPadForOutpainting(
            bitmap.toImagenInlineImage(),
            dimensions,
            position
        )
        return imagenModel.editImage(
            listOf(sourceImage, ImagenRawMask(mask.image!!, 0.05)),
            inputText,
            ImagenEditingConfig(ImagenEditMode.OUTPAINT)
        )
    }
}
