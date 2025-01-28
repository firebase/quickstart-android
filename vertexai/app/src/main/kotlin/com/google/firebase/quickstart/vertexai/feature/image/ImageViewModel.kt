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
import com.google.firebase.vertexai.ImageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewModel(
    private val generativeModel: ImageModel
) : ViewModel() {
    private val _uiState: MutableStateFlow<ImageUiState> = MutableStateFlow(ImageUiState.Initial)
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    fun generateImage(inputText: String) {
        _uiState.value = ImageUiState.Loading

        viewModelScope.launch {
            try {
                val imageResponse = generativeModel.generateImage(inputText)
                val image = imageResponse.images.first()
                _uiState.value = ImageUiState.Success(image.asBitmap())
            } catch (e: Exception) {
                _uiState.value = ImageUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}
