package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.TemplateImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Dimensions
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ImagenBackgroundMask
import com.google.firebase.ai.type.ImagenEditMode
import com.google.firebase.ai.type.ImagenEditingConfig
import com.google.firebase.ai.type.ImagenForegroundMask
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenImagePlacement
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenMaskReference
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenRawImage
import com.google.firebase.ai.type.ImagenRawMask
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.ImagenStyleReference
import com.google.firebase.ai.type.ImagenSubjectReference
import com.google.firebase.ai.type.ImagenSubjectReferenceType
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
open class ImagenViewModel(
    val initialPrompt: String = "",
    val modelName: String = "imagen-4.0-generate-001",
    val backend: GenerativeBackend = GenerativeBackend.googleAI(),
    val includeAttach: Boolean = false,
    val selectionOptions: List<String> = emptyList(),
    val allowEmptyPrompt: Boolean = false,
    val additionalImage: Bitmap? = null,
    val imageLabels: List<String> = emptyList(),
    val editingMode: EditingMode? = null,
    val templateId: String? = null,
    val templateKey: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImagenUiState>(ImagenUiState.Success())
    val uiState: StateFlow<ImagenUiState> = _uiState.asStateFlow()

    // Firebase AI Logic
    private val imagenModel: ImagenModel
    private val templateImagenModel: TemplateImagenModel

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
            backend = backend
        ).imagenModel(
            modelName = modelName,
            generationConfig = config,
            safetySettings = settings
        )
        templateImagenModel = Firebase.ai.templateImagenModel()
    }

    fun generateImages(inputText: String) {
        val currentState = (_uiState.value as? ImagenUiState.Success) ?: ImagenUiState.Success()
        
        viewModelScope.launch {
            _uiState.value = ImagenUiState.Loading
            try {
                val imageResponse = when(editingMode) {
                    EditingMode.INPAINTING -> inpaint(imagenModel, inputText, currentState.attachedImage, currentState.selectedOption)
                    EditingMode.OUTPAINTING -> outpaint(imagenModel, inputText, currentState.attachedImage, currentState.selectedOption)
                    EditingMode.SUBJECT_REFERENCE -> drawReferenceSubject(imagenModel, inputText, currentState.attachedImage)
                    EditingMode.STYLE_TRANSFER -> transferStyle(imagenModel, inputText, currentState.attachedImage)
                    EditingMode.TEMPLATE ->
                        generateWithTemplate(templateImagenModel, templateId!!, mapOf(templateKey!! to inputText))
                    else -> generate(imagenModel, inputText)
                }
                _uiState.value = currentState.copy(images = imageResponse.images.map { it.asBitmap() })
            } catch (e: Exception) {
                val errorMessage =
                    if ((e.localizedMessage?.contains("not found") == true) &&
                        editingMode == EditingMode.TEMPLATE) {
                        "Template was not found, please verify that your project contains a" +
                                " template named \"$templateId\"."
                    } else {
                        e.localizedMessage ?: "Unknown error"
                    }
                _uiState.value = ImagenUiState.Error(errorMessage)
            }
        }
    }

    suspend fun attachImage(
        fileInBytes: ByteArray,
    ) {
        val originalBitmap = BitmapFactory.decodeByteArray(fileInBytes, 0, fileInBytes.size)
        val resizedBitmap = originalBitmap.scale(
            512,
            (originalBitmap.height * (512.0 / originalBitmap.width)).toInt()
        )
        val currentState = (_uiState.value as? ImagenUiState.Success) ?: ImagenUiState.Success()
        _uiState.value = currentState.copy(attachedImage = resizedBitmap)
    }

    fun selectOption(selection: String) {
        val currentState = (_uiState.value as? ImagenUiState.Success) ?: ImagenUiState.Success()
        _uiState.value = currentState.copy(selectedOption = selection)
    }

    private suspend fun transferStyle(
        model: ImagenModel,
        inputText: String,
        attachedImage: Bitmap?
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.editImage(
            listOf(
                ImagenRawImage(MainActivity.catImage.toImagenInlineImage()),
                ImagenStyleReference(attachedImage!!.toImagenInlineImage(), 1, "an art style")
            ),
            "Generate an image in an art style [1] based on the following caption: $inputText",
        )
    }

    private suspend fun drawReferenceSubject(
        model: ImagenModel,
        inputText: String,
        attachedImage: Bitmap?
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.editImage(
            listOf(
                ImagenSubjectReference(
                    referenceId = 1,
                    image = attachedImage!!.toImagenInlineImage(),
                    subjectType = ImagenSubjectReferenceType.ANIMAL,
                    description = "An animal"
                )
            ),
            "Create an image about An animal [1] to match the description: " +
                    inputText.replace("<subject>", "An animal [1]"),
        )
    }

    private suspend fun outpaint(
        model: ImagenModel,
        inputText: String,
        attachedImage: Bitmap?,
        selectedOption: String?
    ): ImagenGenerationResponse<ImagenInlineImage> {
        val bitmap = attachedImage!!
        val position = when (selectedOption) {
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
        return model.editImage(
            listOf(sourceImage, ImagenRawMask(mask.image!!, 0.05)),
            inputText,
            ImagenEditingConfig(ImagenEditMode.OUTPAINT)
        )
    }

    private suspend fun inpaint(
        model: ImagenModel,
        inputText: String,
        attachedImage: Bitmap?,
        selectedOption: String?
    ): ImagenGenerationResponse<ImagenInlineImage> {
        val bitmap = attachedImage!!
        val mask = when (selectedOption) {
            "Foreground" -> ImagenForegroundMask()
            else -> ImagenBackgroundMask()
        }
        return model.editImage(
            listOfNotNull(ImagenRawImage(bitmap.toImagenInlineImage()), mask),
            inputText,
            ImagenEditingConfig(ImagenEditMode.INPAINT_INSERTION)
        )
    }

    private suspend fun generate(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.generateImages(
            inputText
        )
    }

    private suspend fun generateWithTemplate(
        model: TemplateImagenModel,
        templateId: String,
        inputMap: Map<String, String>
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.generateImages(templateId, inputMap)
    }
}
