package com.google.firebase.quickstart.ai

import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.quickstart.ai.ui.navigation.Category
import com.google.firebase.quickstart.ai.ui.navigation.Sample

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
        title = "Translation from audio",
        description = "Translate an audio file",
        navRoute = "chat",
        categories = listOf(Category.AUDIO),
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
        title = "Blog post creator",
        description = "Create a blog post from an image file.",
        navRoute = "chat",
        categories = listOf(Category.IMAGE),
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
        }
    ),
    Sample(
        title = "Gemini 2.0 Flash - image generation",
        description = "Generate and/or edit images using Gemini 2.0 Flash",
        navRoute = "chat",
        categories = listOf(Category.IMAGE),
        modelName = "gemini-2.0-flash-preview-image-generation",
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
        title = "Document comparison",
        description = "Compare the contents of 2 documents",
        navRoute = "chat",
        categories = listOf(Category.DOCUMENT),
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
        title = "Hashtags for a video",
        description = "Generate hashtags for a video ad",
        navRoute = "chat",
        categories = listOf(Category.VIDEO),
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
    )
)
