package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.ai.type.content

val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        title = "Travel tips",
        description = "The user wants the model to help a new traveler with" +
                " travel tips for traveling.",
        navRoute = "chat",
        categories = listOf(Category.TEXT),
        systemInstructions = "You are a Travel assistant. You will answer" +
                " questions the user asks based on the information listed" +
                " in Relevant Information. Do not hallucinate. Do not use" +
                " the internet.",
        history = listOf(
            content {
                role = "user"
                text("I have never traveled before. When should I book a flight?")
            },
            content {
                role = "model"
                text(
                    "You should book flights a couple of months ahead of time." +
                            " It will be cheaper and more flexible for you."
                )
            },
            content {
                role = "user"
                text("Do I need a passport?")
            },
            content {
                role = "model"
                text(
                    "If you are traveling outside your own country, make sure" +
                            " your passport is up-to-date and valid for more" +
                            " than 6 months during your travel."
                )
            },
        ),
        initialPrompt = content { text("What else is important when traveling?") }
    ),
    Sample(
        title = "Chatbot recommendations for courses",
        description = "A chatbot suggests courses for a performing arts program.",
        navRoute = "chat",
        categories = listOf(Category.TEXT)
    ),
    Sample(
        title = "Image generation",
        description = "Generate images with the Imagen 3 model",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content { text("Generate a comicbook style image of Tower bridge with cherry blossoms around") }
    ),
)
