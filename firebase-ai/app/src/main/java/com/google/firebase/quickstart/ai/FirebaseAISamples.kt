package com.google.firebase.quickstart.ai

import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import com.google.firebase.quickstart.ai.ui.navigation.Category
import com.google.firebase.quickstart.ai.ui.navigation.Sample

@OptIn(PublicPreviewAPI::class)
val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        title = "Travel tips",
        description = "The user wants the model to help a new traveler" +
                " with travel tips",
        navRoute = "chat",
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Chatbot recommendations for courses",
        description = "A chatbot suggests courses for a performing arts program.",
        navRoute = "chat",
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Audio Summarization",
        description = "Summarize an audio file",
        navRoute = "chat",
        categories = listOf(Category.AUDIO),
    ),
    Sample(
        title = "Translation from audio (Vertex AI)",
        description = "Translate an audio file stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.AUDIO)
    ),
    Sample(
        title = "Blog post creator (Vertex AI)",
        description = "Create a blog post from an image file stored in Cloud Storage.",
        navRoute = "chat",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 4 - image generation",
        description = "Generate images using Imagen 4",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Inpainting (Vertex AI)",
        description = "Replace part of an image using Imagen 3",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Outpainting (Vertex AI)",
        description = "Expand an image by drawing in more background",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Subject Reference (Vertex AI)",
        description = "Generate an image using a referenced subject (must be an animal)",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Imagen 3 - Style Transfer (Vertex AI)",
        description = "Change the art style of a cat picture using a reference",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Gemini 2.5 Flash Image (aka nanobanana)",
        description = "Generate and/or edit images using Gemini 2.5 Flash Image aka nanobanana",
        navRoute = "chat",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Document comparison (Vertex AI)",
        description = "Compare the contents of 2 documents." +
                " Only supported by the Vertex AI Gemini API because the documents are stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.DOCUMENT)
    ),
    Sample(
        title = "Hashtags for a video (Vertex AI)",
        description = "Generate hashtags for a video ad stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.VIDEO)
    ),
    Sample(
        title = "Summarize video",
        description = "Summarize a video and extract important dialogue.",
        navRoute = "chat",
        categories = listOf(Category.VIDEO)
    ),
    Sample(
        title = "ForecastTalk",
        description = "Use bidirectional streaming to get information about" +
                " weather conditions for a specific US city on a specific date",
        navRoute = "stream",
        categories = listOf(Category.LIVE_API, Category.AUDIO, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Gemini Live (Video input)",
        description = "Use bidirectional streaming to chat with Gemini using your" +
                " phone's camera",
        navRoute = "streamVideo",
        categories = listOf(Category.LIVE_API, Category.VIDEO, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Weather Chat",
        description = "Use function calling to get the weather conditions" +
                " for a specific US city on a specific date.",
        navRoute = "chat",
        categories = listOf(Category.TEXT, Category.FUNCTION_CALLING)
    ),
    Sample(
        title = "Grounding with Google Search",
        description = "Use Grounding with Google Search to get responses based on up-to-date information from the" +
                " web.",
        navRoute = "chat",
        categories = listOf(Category.TEXT, Category.DOCUMENT)
    ),
    Sample(
        title = "Server Prompt Template - Imagen",
        description = "Generate an image using a server prompt template. Note that you need to setup the template in " +
                "the Firebase console before running this demo.",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE)
    ),
    Sample(
        title = "Server Prompt Templates - Gemini",
        description = "Generate an invoice using server prompt templates.  Note that you need to setup the template" +
                " in the Firebase console before running this demo.",
        navRoute = "serverPromptTemplate",
        categories = listOf(Category.TEXT),
    ),
    Sample(
        title = "Thinking",
        description = "Gemini 2.5 Flash with dynamic thinking",
        navRoute = "chat",
        categories = listOf(Category.TEXT)
    ),
    Sample(
        title = "SVG Generator",
        description = "Use Gemini 3 Flash preview to create SVG illustrations",
        navRoute = "svg",
        categories = listOf(Category.IMAGE, Category.TEXT)
    ),
)
