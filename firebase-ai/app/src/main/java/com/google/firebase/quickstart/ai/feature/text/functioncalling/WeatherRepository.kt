package com.google.firebase.quickstart.ai.feature.text.functioncalling

import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionResponsePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Hypothetical repository that calls an external weather API.
 */
class WeatherRepository {

    companion object {
        suspend fun fetchWeather(
            city: String, state: String, date: String
        ): JsonObject = withContext(Dispatchers.IO) {
            // For demo purposes, this hypothetical response is
            // hardcoded here in the expected format.
            return@withContext JsonObject(
                mapOf(
                    "temperature" to JsonPrimitive(38),
                    "chancePrecipitation" to JsonPrimitive("56%"),
                    "cloudConditions" to JsonPrimitive("partlyCloudy")
                )
            )
        }

    }



}