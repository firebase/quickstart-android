/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.vertexai.feature.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.vertexai.ImagenModel
import com.google.firebase.vertexai.type.PublicPreviewAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(PublicPreviewAPI::class)
class ImagenViewModel(
    private val imageModel: ImagenModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<ImagenUiState> =
        MutableStateFlow(ImagenUiState.Initial)
    val uiState: StateFlow<ImagenUiState> = _uiState.asStateFlow()

    fun generateImage(inputText: String) {
        _uiState.value = ImagenUiState.Loading

        viewModelScope.launch {
            // Non-streaming
            try {
                val imageResponse = imageModel.generateImages(
                    inputText
                )
                _uiState.value =
                    ImagenUiState.Success(imageResponse.images.first().asBitmap())
            } catch (e: Exception) {
                _uiState.value = ImagenUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}
