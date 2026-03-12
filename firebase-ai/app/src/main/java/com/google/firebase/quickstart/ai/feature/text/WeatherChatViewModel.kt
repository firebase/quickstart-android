package com.google.firebase.quickstart.ai.feature.text

import kotlinx.serialization.Serializable

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository
import com.google.firebase.quickstart.ai.ui.UiChatMessage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
object WeatherChatRoute

class WeatherChatViewModel : ChatViewModel() {

    override val initialPrompt: String = "What was the weather in Boston, MA on October 17, 2024?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-2.5-flash",
            tools = listOf(
                Tool.functionDeclarations(
                    listOf(
                        FunctionDeclaration(
                            "fetchWeather",
                            "Get the weather conditions for a specific US city on a specific date.",
                            mapOf(
                                "city" to Schema.string("The US city of the location."),
                                "state" to Schema.string("The US state of the location."),
                                "date" to Schema.string(
                                    "The date for which to get the weather." +
                                            " Date must be in the format: YYYY-MM-DD."
                                ),
                            ),
                        )
                    )
                )
            )
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        if (response.functionCalls.isEmpty()) {
            validateAndDisplayResponse(response, currentMessages)
        } else {
            handleFunctionCalls(response, currentMessages)
        }
    }

    private suspend fun handleFunctionCalls(
        response: GenerateContentResponse,
        currentMessages: List<UiChatMessage>
    ) {
        response.functionCalls.forEach { functionCall ->
            Log.d(
                "WeatherChatViewModel", "Model responded with function call:" +
                        functionCall.name
            )
            when (functionCall.name) {
                "fetchWeather" -> {
                    val city = functionCall.args["city"]?.jsonPrimitive?.content
                    val state = functionCall.args["state"]?.jsonPrimitive?.content // Fixed state retrieval
                    val date = functionCall.args["date"]?.jsonPrimitive?.content

                    val finalResponse = if (city == null || state == null || date == null) {
                        chat.sendMessage(content("function") {
                            part(FunctionResponsePart("fetchWeather",
                                JsonObject(
                                    mapOf(
                                        "error" to JsonPrimitive("Unable to fetch weather - one of the parameters was null"),
                                    )
                                )))
                        })
                    } else {
                        val functionResponse = WeatherRepository
                            .fetchWeather(city, state, date)

                        chat.sendMessage(content("function") {
                            part(FunctionResponsePart("fetchWeather", functionResponse))
                        })
                    }

                    validateAndDisplayResponse(finalResponse, currentMessages)
                }
            }
        }
    }
}

