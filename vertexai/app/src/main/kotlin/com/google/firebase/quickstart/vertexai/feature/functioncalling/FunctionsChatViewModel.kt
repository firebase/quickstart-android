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

package com.google.firebase.quickstart.vertexai.feature.functioncalling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.FunctionResponsePart
import com.google.firebase.vertexai.type.InvalidStateException
import com.google.firebase.vertexai.type.asTextOrNull
import com.google.firebase.vertexai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.lang.IllegalArgumentException

class FunctionsChatViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") {
                text("Hello, what can you do?.")
            },
            content(role = "model") {
                text("Great to meet you. I can return the upper case version of the text you send me")
            }
        )
    )

    private val _uiState: MutableStateFlow<FunctionsChatUiState> =
        MutableStateFlow(
            FunctionsChatUiState(
                chat.history.map { content ->
                    // Map the initial messages
                    FunctionsChatMessage(
                        text = content.parts.first().asTextOrNull() ?: "",
                        participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                        isPending = false
                    )
                }
            )
        )
    val uiState: StateFlow<FunctionsChatUiState> =
        _uiState.asStateFlow()

    fun sendMessage(userMessage: String) {
        // Add a pending message
        _uiState.value.addMessage(
            FunctionsChatMessage(
                text = userMessage,
                participant = Participant.USER,
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                var response =
                    chat.sendMessage("What would be the uppercase representation of the following text: $userMessage")

                // Getting the first matched function call
                val firstFunctionCall = response.functionCalls.firstOrNull()

                if (firstFunctionCall != null) {
                    val functionCall = firstFunctionCall
                    val result = when (functionCall.name) {
                        "upperCase" -> buildJsonObject {
                            put(
                                "result",
                                JsonPrimitive(functionCall.args["text"].toString().uppercase() ?: "")
                            )
                        }

                        else -> throw IllegalArgumentException(
                            "Model requested nonexistent function \"${firstFunctionCall.name}\" "
                        )
                    }

                    response = chat.sendMessage(
                        content(role = "function") {
                            part(FunctionResponsePart("upperCase", result))
                        }
                    )
                }

                _uiState.value.replaceLastPendingMessage()

                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        FunctionsChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                            isPending = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    FunctionsChatMessage(
                        text = e.localizedMessage,
                        participant = Participant.ERROR
                    )
                )
            }
        }
    }
}
