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

@OptIn(PublicPreviewAPI::class)
class ImagenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ImagenRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt = sample.initialPrompt?.parts?.first()?.asTextOrNull().orEmpty()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _includeAttach = MutableStateFlow(sample.includeAttach)
    val includeAttach: StateFlow<Boolean> = _includeAttach

    private val _selectionOptions = MutableStateFlow(sample.selectionOptions)
    val selectionOptions: StateFlow<List<String>> = _selectionOptions

    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption

    private val _allowEmptyPrompt = MutableStateFlow(sample.allowEmptyPrompt)
    val allowEmptyPrompt = _allowEmptyPrompt

    private val _additionalImage = MutableStateFlow(sample.additionalImage)
    val additionalImage: StateFlow<Bitmap?> = _additionalImage

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
                val imageResponse =
                    sample.generateImages!!(imagenModel, inputText, attachedImage.first(), selectedOption.first())
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
        val resizedBitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            512,
            (originalBitmap.height * (512.0 / originalBitmap.width)).toInt(),
            true
        )
        _attachedImage.emit(resizedBitmap)
    }

    fun selectOption(selection: String) {
        viewModelScope.launch {
            _selectedOption.emit(selection)
        }
    }
}
