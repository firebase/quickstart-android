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
abstract class ImagenViewModel : ViewModel() {

    abstract val initialPrompt: String
    abstract val includeAttach: Boolean
    abstract val selectionOptions: List<String>
    abstract val allowEmptyPrompt: Boolean
    abstract val additionalImage: Bitmap?
    abstract val imageLabels: List<String>

    private val _uiState = MutableStateFlow<ImagenUiState>(ImagenUiState.Success())
    val uiState: StateFlow<ImagenUiState> = _uiState.asStateFlow()

    protected abstract suspend fun performGeneration(inputText: String, currentState: ImagenUiState.Success): ImagenGenerationResponse<ImagenInlineImage>

    fun generateImages(inputText: String) {
        val currentState = (_uiState.value as? ImagenUiState.Success) ?: ImagenUiState.Success()
        
        viewModelScope.launch {
            _uiState.value = ImagenUiState.Loading
            try {
                val imageResponse = performGeneration(inputText, currentState)
                _uiState.value = currentState.copy(images = imageResponse.images.map { it.asBitmap() })
            } catch (e: Exception) {
                _uiState.value = ImagenUiState.Error(e.localizedMessage ?: "Unknown error")
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
}
