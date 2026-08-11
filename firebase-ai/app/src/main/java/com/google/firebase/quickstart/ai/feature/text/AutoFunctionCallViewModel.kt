package com.google.firebase.quickstart.ai.feature.text

import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AutoFunctionDeclaration
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FirebaseAutoFunctionException
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.JsonSchema
import com.google.firebase.ai.type.Tool
import com.google.firebase.quickstart.ai.feature.text.functioncalling.WeatherRepository
import com.google.firebase.quickstart.ai.ui.UiChatMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
object AutoFunctionCallRoute

class AutoFunctionCallViewModel : ChatViewModel() {

    override val initialPrompt: String = "What was the weather in Boston, MA on October 17, 2024?"

    private val chat: Chat

    init {
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.5-flash-lite",
            tools = listOf(
                Tool.functionDeclarations(
                    autoFunctionDeclarations = listOf(
                        AutoFunctionDeclaration.create(
                            functionName = "fetchWeather",
                            description = "Get the weather conditions for a specific US city on a specific date.",
                            inputSchema = JsonSchema.obj(
                                properties = mapOf(
                                    "city" to JsonSchema.string("The US city of the location."),
                                    "state" to JsonSchema.string("The US state of the location."),
                                    "date" to JsonSchema.string(
                                        "The date for which to get the weather." +
                                                " Date must be in the format: YYYY-MM-DD."
                                    ),
                                )
                            ),
                            functionReference = ::fetchWeather
                        )
                    )
                )
            )
        )
        chat = generativeModel.startChat()
    }

    override suspend fun performSendMessage(prompt: Content, currentMessages: List<UiChatMessage>) {
        val response = chat.sendMessage(prompt)
        validateAndDisplayResponse(response, currentMessages)
    }

    private suspend fun fetchWeather(input: JsonObject): FunctionResponsePart {
        val city = input["city"]?.jsonPrimitive?.content
        val state = input["state"]?.jsonPrimitive?.content
        val date = input["date"]?.jsonPrimitive?.content

        return if (city == null || state == null || date == null) {
            // Tell the model there was an error
            throw FirebaseAutoFunctionException("Unable to fetch weather - one of the parameters was null")
        } else {
            // Execute the function call and return the response
            val functionResponse = WeatherRepository
                .fetchWeather(city, state, date)

            FunctionResponsePart("fetchWeather", functionResponse)
        }
    }
}

