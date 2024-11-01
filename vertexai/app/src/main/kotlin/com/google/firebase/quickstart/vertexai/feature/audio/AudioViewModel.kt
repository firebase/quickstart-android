/*
 * Copyright 2024 Google LLC
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

package com.google.firebase.quickstart.vertexai.feature.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<AudioUiState> = MutableStateFlow(AudioUiState.Initial)
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()
    val audioRecorder = AudioRecorder()

    fun reason(
        userInput: String,
        audioBytes: ByteArray,
    ) {
        _uiState.value = AudioUiState.Loading
        val prompt = if (userInput.isBlank()) {
          "Answer the question in the audio."
        } else {
          "Listen to the audio, and then answer the following question: $userInput"
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputContent = content {
                    inlineData(audioBytes, "audio/aac")
                    text(prompt)
                }

                var outputContent = ""

                generativeModel.generateContentStream(inputContent).collect { response ->
                    outputContent += response.text
                    _uiState.value = AudioUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = AudioUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}
