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
import com.google.firebase.ai.type.ImagenRawImage
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.ImagenStyleReference
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.MainActivity
import com.google.firebase.quickstart.ai.ui.ImagenUiState
import kotlinx.serialization.Serializable

@Serializable
object ImagenStyleTransferRoute

@OptIn(PublicPreviewAPI::class)
class ImagenStyleTransferViewModel : ImagenViewModel() {
    override val initialPrompt: String = "A picture of a cat"
    override val includeAttach: Boolean = true
    override val selectionOptions: List<String> = emptyList()
    override val allowEmptyPrompt: Boolean = true
    override val additionalImage: Bitmap? = MainActivity.catImage
    override val imageLabels: List<String> = listOf("Style Target", "Style Source")

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
        val attachedImage = currentState.attachedImage!!
        return imagenModel.editImage(
            listOf(
                ImagenRawImage(MainActivity.catImage.toImagenInlineImage()),
                ImagenStyleReference(attachedImage.toImagenInlineImage(), 1, "an art style")
            ),
            "Generate an image in an art style [1] based on the following caption: $inputText",
        )
    }
}
