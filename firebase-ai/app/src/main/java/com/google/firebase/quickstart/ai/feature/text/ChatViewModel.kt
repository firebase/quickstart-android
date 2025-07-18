package com.google.firebase.quickstart.ai.feature.text

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FileDataPart
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GroundingMetadata
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.asTextOrNull
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.FIREBASE_AI_SAMPLES
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

/**
 * A wrapper for a model [Content] object that includes additional UI-specific metadata.
 */
data class UiChatMessage(
    val content: Content,
    val groundingMetadata: GroundingMetadata? = null,
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sampleId = savedStateHandle.toRoute<ChatRoute>().sampleId
    private val sample = FIREBASE_AI_SAMPLES.first { it.id == sampleId }
    val initialPrompt: String =
        sample.initialPrompt?.parts
            ?.filterIsInstance<TextPart>()
            ?.first()
            ?.asTextOrNull().orEmpty()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _messageList: MutableList<UiChatMessage> =
        sample.chatHistory.map { UiChatMessage(it) }.toMutableStateList()
    private val _messages = MutableStateFlow<List<UiChatMessage>>(_messageList)
    val messages: StateFlow<List<UiChatMessage>> =
        _messages

    private val _attachmentsList: MutableList<Attachment> =
        sample.initialPrompt?.parts?.filterIsInstance<FileDataPart>()?.map {
            Attachment(it.uri)
        }?.toMutableStateList() ?: mutableStateListOf()
    private val _attachments = MutableStateFlow<List<Attachment>>(_attachmentsList)
    val attachments: StateFlow<List<Attachment>>
        get() = _attachments

    // Firebase AI Logic
    private var contentBuilder = Content.Builder()
    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = sample.backend // GenerativeBackend.googleAI() by default
        ).generativeModel(
            modelName = sample.modelName ?: "gemini-2.5-flash",
            systemInstruction = sample.systemInstructions,
            generationConfig = sample.generationConfig,
            tools = sample.tools
        )
        chat = generativeModel.startChat(sample.chatHistory)

        // add attachments from initial prompt
        sample.initialPrompt?.parts?.forEach { part ->
            if (part is TextPart) {
                /* Ignore text parts, as the text will come from the textInputField */
            } else {
                contentBuilder.part(part)
            }
        }
    }

    fun sendMessage(userMessage: String) {
        val prompt = contentBuilder
            .text(userMessage)
            .build()

        _messageList.add(UiChatMessage(prompt))

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(prompt)
                if (response.functionCalls.isEmpty()) {
                    // Samples without function calling can display the response in the UI
                    val candidate = response.candidates.first()

                    // Compliance check for grounding
                    if (candidate.groundingMetadata != null
                        && candidate.groundingMetadata?.groundingChunks?.isNotEmpty() == true
                        && candidate.groundingMetadata?.searchEntryPoint == null) {
                        _errorMessage.value =
                            "Could not display the response because it was missing required attribution components."
                    } else {
                        _messageList.add(
                            UiChatMessage(candidate.content, candidate.groundingMetadata)
                        )
                        _errorMessage.value = null // clear errors
                    }
                } else {
                    // Samples WITH function calling need to perform
                    // additional handling
                    handleFunctionCalls(response)
                }
                _errorMessage.value = null // clear errors
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
                contentBuilder = Content.Builder() // reset the builder
                _attachmentsList.clear()
            }
        }
    }

    fun attachFile(
        fileInBytes: ByteArray,
        mimeType: String?,
        fileName: String? = "Unnamed file"
    ) {
        if (mimeType?.contains("image") == true) {
            // images should be attached as ImageParts
            contentBuilder.image(decodeBitmapFromImage(fileInBytes))
        } else {
            contentBuilder.inlineData(fileInBytes, mimeType ?: "text/plain")
        }
        _attachmentsList.add(Attachment(fileName ?: "Unnamed file"))
    }

    /**
     * Only used by samples with function calling
     */
    private suspend fun handleFunctionCalls(
        response: GenerateContentResponse
    ) {
        response.functionCalls.forEach { functionCall ->
            Log.d(
                "ChatViewModel", "Model responded with function call:" +
                        functionCall.name
            )
            when (functionCall.name) {
                "fetchWeather" -> {
                    // Handle the call to fetchWeather()
                    val city = functionCall.args["city"]!!.jsonPrimitive.content
                    val state = functionCall.args["city"]!!.jsonPrimitive.content
                    val date = functionCall.args["date"]!!.jsonPrimitive.content

                    val functionResponse = WeatherRepository
                        .fetchWeather(city, state, date)

                    // Send the response(s) from the function back to the model
                    // so that the model can use it to generate its final response.
                    val finalResponse = chat.sendMessage(content("function") {
                        part(FunctionResponsePart("fetchWeather", functionResponse))
                    })

                    Log.d("ChatViewModel", "Model responded with: ${finalResponse.text}")
                    val candidate = finalResponse.candidates.first()
                    _messageList.add(UiChatMessage(candidate.content,
                        candidate.groundingMetadata))
                }

                else -> {
                    Log.d(
                        "ChatViewModel", "Model responded with unknown" +
                                " function call: ${functionCall.name}"
                    )
                }
            }
        }
    }

    private fun decodeBitmapFromImage(input: ByteArray) =
        BitmapFactory.decodeByteArray(input, 0, input.size)
}
