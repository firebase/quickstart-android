package com.google.firebase.quickstart.ai.feature.media.imagen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.ImagenModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagenImageFormat
import com.google.firebase.ai.type.ImagenPersonFilterLevel
import com.google.firebase.ai.type.ImagenSafetyFilterLevel
import com.google.firebase.ai.type.ImagenSafetySettings
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.ai.type.imagenGenerationConfig
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import com.google.firebase.ai.type.Dimensions
import com.google.firebase.ai.type.ImagenBackgroundMask
import com.google.firebase.ai.type.ImagenEditMode
import com.google.firebase.ai.type.ImagenEditingConfig
import com.google.firebase.ai.type.ImagenForegroundMask
import com.google.firebase.ai.type.ImagenGenerationResponse
import com.google.firebase.ai.type.ImagenImagePlacement
import com.google.firebase.ai.type.ImagenInlineImage
import com.google.firebase.ai.type.ImagenMaskReference
import com.google.firebase.ai.type.ImagenRawImage
import com.google.firebase.ai.type.ImagenRawMask
import com.google.firebase.ai.type.ImagenStyleReference
import com.google.firebase.ai.type.ImagenSubjectReference
import com.google.firebase.ai.type.ImagenSubjectReferenceType
import com.google.firebase.ai.type.toImagenInlineImage
import com.google.firebase.quickstart.ai.MainActivity
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(PublicPreviewAPI::class)
class ImagenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ImagenRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()
    val imageLabels = sample.imageLabels

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val includeAttach = sample.includeAttach

    val selectionOptions = sample.selectionOptions

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption

    val allowEmptyPrompt = sample.allowEmptyPrompt

    val additionalImage = sample.additionalImage

    private val _attachedImage = MutableStateFlow<Bitmap?>(null)
    val attachedImage: StateFlow<Bitmap?> = _attachedImage

    private val _generatedBitmaps = MutableStateFlow(listOf<Bitmap>())
    val generatedBitmaps: StateFlow<List<Bitmap>> = _generatedBitmaps

    // Firebase AI Logic
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
            backend = sample.backend
        ).imagenModel(
            modelName = sample.modelName ?: "imagen-3.0-generate-002",
            generationConfig = config,
            safetySettings = settings
        )
    }

    fun generateImages(inputText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageResponse = when(sample.editingMode) {
                    EditingMode.INPAINTING -> inpaint(imagenModel, inputText)
                    EditingMode.OUTPAINTING -> outpaint(imagenModel, inputText)
                    EditingMode.SUBJECT_REFERENCE -> drawReferenceSubject(imagenModel, inputText)
                    EditingMode.STYLE_TRANSFER -> transferStyle(imagenModel, inputText)
                    else -> generate(imagenModel, inputText)
                }
                _generatedBitmaps.value = imageResponse.images.map { it.asBitmap() }
                _errorMessage.value = null // clear error message
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
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
        _attachedImage.emit(resizedBitmap)
    }

    fun selectOption(selection: String) {
        viewModelScope.launch {
            _selectedOption.emit(selection)
        }
    }

    suspend fun transferStyle(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.editImage(
            listOf(
                ImagenRawImage(MainActivity.catImage.toImagenInlineImage()),
                ImagenStyleReference(attachedImage.first()!!.toImagenInlineImage(), 1, "an art style")
            ),
            "Generate an image in an art style [1] based on the following caption: $inputText",
        )
    }

    suspend fun drawReferenceSubject(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.editImage(
            listOf(
                ImagenSubjectReference(
                    referenceId = 1,
                    image = attachedImage.first()!!.toImagenInlineImage(),
                    subjectType = ImagenSubjectReferenceType.ANIMAL,
                    description = "An animal"
                )
            ),
            "Create an image about An animal [1] to match the description: " +
                    inputText.replace("<subject>", "An animal [1]"),
        )
    }

    suspend fun outpaint(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        val bitmap = attachedImage.first()!!
        val position = when (selectedOption.first()) {
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

    suspend fun inpaint(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        val bitmap = attachedImage.first()!!
        val mask = when (selectedOption.first()) {
            "Foreground" -> ImagenForegroundMask()
            else -> ImagenBackgroundMask()
        }
        return model.editImage(
            listOfNotNull(ImagenRawImage(bitmap.toImagenInlineImage()), mask),
            inputText,
            ImagenEditingConfig(ImagenEditMode.INPAINT_INSERTION)
        )
    }

    suspend fun generate(
        model: ImagenModel,
        inputText: String,
    ): ImagenGenerationResponse<ImagenInlineImage> {
        return model.generateImages(
            inputText
        )
    }
}
