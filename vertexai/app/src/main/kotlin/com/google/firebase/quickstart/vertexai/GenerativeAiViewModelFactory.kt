/*
 * Copyright 2023 Google LLC
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

package com.google.firebase.quickstart.vertexai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.Firebase
import com.google.firebase.quickstart.vertexai.feature.audio.AudioViewModel
import com.google.firebase.quickstart.vertexai.feature.chat.ChatViewModel
import com.google.firebase.quickstart.vertexai.feature.functioncalling.FunctionsChatViewModel
import com.google.firebase.quickstart.vertexai.feature.image.ImagenViewModel
import com.google.firebase.quickstart.vertexai.feature.multimodal.PhotoReasoningViewModel
import com.google.firebase.quickstart.vertexai.feature.text.SummarizeViewModel
import com.google.firebase.vertexai.type.FunctionDeclaration
import com.google.firebase.vertexai.type.ImagenAspectRatio
import com.google.firebase.vertexai.type.ImagenImageFormat
import com.google.firebase.vertexai.type.ImagenPersonFilterLevel
import com.google.firebase.vertexai.type.ImagenSafetyFilterLevel
import com.google.firebase.vertexai.type.ImagenSafetySettings
import com.google.firebase.vertexai.type.PublicPreviewAPI
import com.google.firebase.vertexai.type.SafetySetting
import com.google.firebase.vertexai.type.Schema
import com.google.firebase.vertexai.type.Tool
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.type.imagenGenerationConfig
import com.google.firebase.vertexai.vertexAI

@OptIn(PublicPreviewAPI::class)
val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        viewModelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val config = generationConfig {
            temperature = 0.7f
        }

        return with(viewModelClass) {
            when {
                isAssignableFrom(SummarizeViewModel::class.java) -> {
                    // Initialize a GenerativeModel with a Gemini model
                    // for text generation
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    SummarizeViewModel(generativeModel)
                }

                isAssignableFrom(PhotoReasoningViewModel::class.java) -> {
                    // Initialize a GenerativeModel with a Gemini model
                    // for multimodal text generation
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    PhotoReasoningViewModel(generativeModel)
                }

                isAssignableFrom(ChatViewModel::class.java) -> {
                    // Initialize a GenerativeModel with a Gemini model for chat
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    ChatViewModel(generativeModel)
                }

                isAssignableFrom(FunctionsChatViewModel::class.java) -> {
                    // Declare the functions you want to make available to the model
                    val functionDeclaration = FunctionDeclaration(
                        name = "upperCase",
                        description = "Returns the upper case version of the input string",
                        parameters = mapOf(
                            "input" to Schema.string( "Text to transform"))
                    )
                    val tools = listOf(
                        Tool.functionDeclarations(
                            listOf(
                                functionDeclaration
                            )
                        )
                    )


                    // Initialize a GenerativeModel with a Gemini model for function calling chat
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config,
                        tools = tools
                    )
                    FunctionsChatViewModel(generativeModel)
                }

                isAssignableFrom(AudioViewModel::class.java) -> {
                    // Initialize a GenerativeModel with a Gemini model for audio understanding
                    val generativeModel = Firebase.vertexAI.generativeModel(
                        modelName = "gemini-2.0-flash",
                        generationConfig = config
                    )
                    AudioViewModel(generativeModel)
                }

                isAssignableFrom(ImagenViewModel::class.java) -> {
                    val generationConfig = imagenGenerationConfig {
                        numberOfImages = 1
                        aspectRatio = ImagenAspectRatio.PORTRAIT_3x4
                        imageFormat = ImagenImageFormat.png()
                    }
                    val safetySettings = ImagenSafetySettings(
                        safetyFilterLevel = ImagenSafetyFilterLevel.BLOCK_LOW_AND_ABOVE,
                        personFilterLevel = ImagenPersonFilterLevel.BLOCK_ALL
                    )
                    val imagenModel = Firebase.vertexAI.imagenModel(
                        "imagen-3.0-generate-002", generationConfig, safetySettings)
                    ImagenViewModel(imagenModel)
                }

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${viewModelClass.name}")
            }
        } as T
    }
}
