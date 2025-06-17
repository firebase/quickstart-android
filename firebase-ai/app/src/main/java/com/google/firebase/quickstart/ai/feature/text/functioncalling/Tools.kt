package com.google.firebase.quickstart.ai.feature.text.functioncalling

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema

val fetchWeatherTool = FunctionDeclaration(
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
