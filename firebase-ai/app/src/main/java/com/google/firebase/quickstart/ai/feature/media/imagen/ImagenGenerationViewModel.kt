package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.google.firebase.ai.type.toImagenInlineImage
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

    private val generativeModel: GenerativeModel

    init {
        generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.1-flash-image-preview",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
//                candidateCount = 4
                thinkingConfig = thinkingConfig {
                    thinkingLevel = ThinkingLevel.MINIMAL
                }
            }
        )
    }

    override suspend fun performGeneration(
        inputText: String,
        currentState: ImagenUiState.Success
    ): List<Bitmap> {
        val bitmaps = arrayListOf<Bitmap>()
        generativeModel.generateContent(inputText)
            .candidates.forEach { candidate ->
                candidate.content.parts.filterIsInstance<ImagePart>()
                    .forEach {
                        bitmaps.add(it.image)
                    }
            }

        return bitmaps
//        return imagenModel.generateImages(inputText)
    }
}
