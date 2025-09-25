package com.google.firebase.quickstart.ai

import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.quickstart.ai.feature.media.imagen.EditingMode
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
        systemInstructions = content {
            text(
                "You are a Travel assistant. You will answer" +
                        " questions the user asks based on the information listed" +
                        " in Relevant Information. Do not hallucinate. Do not use" +
                        " the internet."
            )
        },
        chatHistory = listOf(
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
            }
        ),
        initialPrompt = content { text("What else is important when traveling?") }
    ),
    Sample(
        title = "Chatbot recommendations for courses",
        description = "A chatbot suggests courses for a performing arts program.",
        navRoute = "chat",
        categories = listOf(Category.TEXT),
        systemInstructions = content {
            text(
                "You are a chatbot for the county's performing and fine arts" +
                        " program. You help students decide what course they will" +
                        " take during the summer."
            )
        },
        initialPrompt = content {
            text("I am interested in Performing Arts. I have taken Theater 1A.")
        }
    ),
    Sample(
        title = "Audio Summarization",
        description = "Summarize an audio file",
        navRoute = "chat",
        categories = listOf(Category.AUDIO),
        chatHistory = listOf(
            content { text("Can you help me summarize an audio file?") },
            content("model") {
                text(
                    "Of course! Click on the attach button" +
                            " below and choose an audio file for me to summarize."
                )
            }
        ),
        initialPrompt = content {
            text(
                "I have attached the audio file. Please analyze it and summarize the contents" +
                        " of the audio as bullet points."
            )
        }
    ),
    Sample(
        title = "Translation from audio (Vertex AI)",
        description = "Translate an audio file stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.AUDIO),
        backend = GenerativeBackend.vertexAI(),
        initialPrompt = content {
            fileData(
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/audio/" +
                        "How_to_create_a_My_Map_in_Google_Maps.mp3",
                "audio/mpeg"
            )
            text("Please translate the audio to Mandarin.")
        }
    ),
    Sample(
        title = "Blog post creator (Vertex AI)",
        description = "Create a blog post from an image file stored in Cloud Storage.",
        navRoute = "chat",
        categories = listOf(Category.IMAGE),
        backend = GenerativeBackend.vertexAI(),
        initialPrompt = content {
            fileData(
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/image/meal-prep.jpeg",
                "image/jpeg"
            )
            text(
                "Write a short, engaging blog post based on this picture." +
                        " It should include a description of the meal in the" +
                        " photo and talk about my journey meal prepping."
            )
        }
    ),
    Sample(
        title = "Imagen 3 - image generation",
        description = "Generate images using Imagen 3",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content {
            text(
                "A photo of a modern building with water in the background"
            )
        },
        allowEmptyPrompt = false,
        editingMode = EditingMode.GENERATE,
    ),
    Sample(
        title = "Imagen 3 - Inpainting (Vertex AI)",
        description = "Replace part of an image using Imagen 3",
        modelName = "imagen-3.0-capability-001",
        backend = GenerativeBackend.vertexAI(),
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content { text("A sunny beach") },
        includeAttach = true,
        allowEmptyPrompt = true,
        selectionOptions = listOf("Mask", "Background", "Foreground"),
        editingMode = EditingMode.INPAINTING,
    ),
    Sample(
        title = "Imagen 3 - Outpainting (Vertex AI)",
        description = "Expand an image by drawing in more background",
        modelName = "imagen-3.0-capability-001",
        backend = GenerativeBackend.vertexAI(),
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content { text("") },
        includeAttach = true,
        allowEmptyPrompt = true,
        selectionOptions = listOf("Image Alignment", "Center", "Top", "Bottom", "Left", "Right"),
        editingMode = EditingMode.OUTPAINTING,
    ),
    Sample(
        title = "Imagen 3 - Subject Reference (Vertex AI)",
        description = "Generate an image using a referenced subject (must be an animal)",
        modelName = "imagen-3.0-capability-001",
        backend = GenerativeBackend.vertexAI(),
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content { text("<subject> flying through space") },
        includeAttach = true,
        allowEmptyPrompt = false,
        editingMode = EditingMode.SUBJECT_REFERENCE,
    ),
    Sample(
        title = "Imagen 3 - Style Transfer (Vertex AI)",
        description = "Change the art style of a cat picture using a reference",
        modelName = "imagen-3.0-capability-001",
        backend = GenerativeBackend.vertexAI(),
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content { text("A picture of a cat") },
        includeAttach = true,
        allowEmptyPrompt = true,
        additionalImage = MainActivity.catImage,
        imageLabels = listOf("Style Target", "Style Source"),
        editingMode = EditingMode.STYLE_TRANSFER
    ),
    Sample(
        title = "Gemini 2.5 Flash Image (aka nanobanana)",
        description = "Generate and/or edit images using Gemini 2.5 Flash Image aka nanobanana",
        navRoute = "chat",
        categories = listOf(Category.IMAGE),
        modelName = "gemini-2.5-flash-image",
        initialPrompt = content {
            text(
                "Hi, can you create a 3d rendered image of a pig " +
                        "with wings and a top hat flying over a happy " +
                        "futuristic scifi city with lots of greenery?"
            )
        },
        generationConfig = generationConfig {
            responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
        }
    ),
    Sample(
        title = "Document comparison (Vertex AI)",
        description = "Compare the contents of 2 documents." +
                " Only supported by the Vertex AI Gemini API because the documents are stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.DOCUMENT),
        backend = GenerativeBackend.vertexAI(),
        initialPrompt = content {
            fileData(
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/pdf/form_1040_2013.pdf",
                "application/pdf"
            )
            fileData(
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/pdf/form_1040_2023.pdf",
                "application/pdf"
            )
            text(
                "The first document is from 2013, and the second document is" +
                        " from 2023. How did the standard deduction evolve?"
            )
        }
    ),
    Sample(
        title = "Hashtags for a video (Vertex AI)",
        description = "Generate hashtags for a video ad stored in Cloud Storage",
        navRoute = "chat",
        categories = listOf(Category.VIDEO),
        backend = GenerativeBackend.vertexAI(),
        initialPrompt = content {
            fileData(
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/video/google_home_celebrity_ad.mp4",
                "video/mpeg"
            )
            text(
                "Generate 5-10 hashtags that relate to the video content." +
                        " Try to use more popular and engaging terms," +
                        " e.g. #Viral. Do not add content not related to" +
                        " the video.\n Start the output with 'Tags:'"
            )
        }
    ),
    Sample(
        title = "Summarize video",
        description = "Summarize a video and extract important dialogue.",
        navRoute = "chat",
        categories = listOf(Category.VIDEO),
        chatHistory = listOf(
            content { text("Can you help me with the description of a video file?") },
            content("model") {
                text(
                    "Sure! Click on the attach button below and choose a" +
                            " video file for me to describe."
                )
            }
        ),
        initialPrompt = content {
            text(
                "I have attached the video file. Provide a description of" +
                        " the video. The description should also contain" +
                        " anything important which people say in the video."
            )
        }
    ),
    Sample(
        title = "ForecastTalk",
        description = "Use bidirectional streaming to get information about" +
                " weather conditions for a specific US city on a specific date",
        navRoute = "stream",
        categories = listOf(Category.LIVE_API, Category.AUDIO, Category.FUNCTION_CALLING),
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
        ),
        initialPrompt = content {
            text("What was the weather in Boston, MA on October 17, 2024?")
        }
    ),
    Sample(
        title = "Weather Chat",
        description = "Use function calling to get the weather conditions" +
                " for a specific US city on a specific date.",
        navRoute = "chat",
        categories = listOf(Category.TEXT, Category.FUNCTION_CALLING),
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
        ),
        initialPrompt = content {
            text("What was the weather in Boston, MA on October 17, 2024?")
        }
    ),
    Sample(
        title = "Grounding with Google Search",
        description = "Use Grounding with Google Search to get responses based on up-to-date information from the web.",
        navRoute = "chat",
        categories = listOf(Category.TEXT, Category.DOCUMENT),
        modelName = "gemini-2.5-flash",
        tools = listOf(Tool.googleSearch()),
        initialPrompt = content {
            text(
                "What's the weather in Chicago this weekend?"
            )
        },
    ),
)
