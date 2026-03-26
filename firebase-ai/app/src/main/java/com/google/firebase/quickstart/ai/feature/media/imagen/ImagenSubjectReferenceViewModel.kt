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
import com.google.firebase.ai.type.ImagenSubjectReference
import com.google.firebase.ai.type.ImagenSubjectReferenceType
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.ui.ImagenUiState
import kotlinx.serialization.Serializable

@Serializable
object ImagenSubjectReferenceRoute

@OptIn(PublicPreviewAPI::class)
class ImagenSubjectReferenceViewModel : ImagenViewModel() {
    override val initialPrompt: String = "<subject> flying through space"
    override val includeAttach: Boolean = true
    override val selectionOptions: List<String> = emptyList()
    override val allowEmptyPrompt: Boolean = false
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
    ): List<Bitmap> {
        val attachedImage = currentState.attachedImage!!
        return imagenModel.editImage(
            listOf(
                ImagenSubjectReference(
                    referenceId = 1,
                    image = attachedImage.toImagenInlineImage(),
                    subjectType = ImagenSubjectReferenceType.ANIMAL,
                    description = "An animal"
                )
            ),
            "Create an image about An animal [1] to match the description: " +
                    inputText.replace("<subject>", "An animal [1]"),
        ).images.map { it.asBitmap() }
    }
}
