package com.google.firebase.quickstart.ai.ui.navigation

val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        "Travel tips",
        "The user wants the model to help a new traveler with travel tips for traveling.",
        "post",
        listOf(Category.TEXT)
    ),
    Sample(
        "Chatbot recommendations for courses",
        "A chatbot suggests courses for a performing arts program.",
        "post",
        listOf(Category.TEXT)
    ),
    Sample(
        "Extract data from receipts",
        "Analyze an image of a receipt and extract data in JSON format",
        "post",
        listOf(Category.IMAGE)
    ),
    Sample(
        "Describe video content",
        "Get a description of the contents of a video",
        "post",
        listOf(Category.VIDEO)
    ),
    Sample(
        "Audio diarization",
        "Segment an audio record by speaker labels",
        "post",
        listOf(Category.AUDIO)
    ),
    Sample(
        "Function calling",
        "Ask Gemini about the current weather",
        "post",
        listOf(Category.FUNCTION_CALL)
    ),
    Sample(
        "Image generation",
        "Generate images with the Imagen 3 model",
        "imagen",
        listOf(Category.IMAGE)
    ),
)
