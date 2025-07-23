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
import com.google.firebase.ai.type.ImagenAspectRatio
import com.google.firebase.ai.type.ImagenEditMode
import com.google.firebase.ai.type.ImagenEditingConfig
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

    private val _allowEmptyPrompt = MutableStateFlow(sample.allowEmptyPrompt)
    val allowEmptyPrompt: StateFlow<Boolean> = _allowEmptyPrompt

    private val _generatedBitmaps = MutableStateFlow(listOf<Bitmap>())
    val generatedBitmaps: StateFlow<List<Bitmap>> = _generatedBitmaps

    // Firebase AI Logic
    private val imagenModel: ImagenModel
    private var attachedImage: Bitmap?

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
        attachedImage = null
    }

    fun generateImages(inputText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageResponse = sample.generateImages!!(imagenModel, inputText, attachedImage)
                _generatedBitmaps.value = imageResponse.images.map { it.asBitmap() }
                _errorMessage.value = null // clear error message
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun attachImage(
        fileInBytes: ByteArray,
    ) {
        attachedImage = BitmapFactory.decodeByteArray(fileInBytes, 0, fileInBytes.size)
    }
}
