package com.google.firebase.quickstart.ai.ui.navigation

import com.google.firebase.vertexai.type.content

val FIREBASE_AI_SAMPLES = listOf(
    Sample(
        title = "Travel tips",
        description = "The user wants the model to help a new traveler with" +
                " travel tips for traveling.",
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
            },
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
                "https://storage.googleapis.com/cloud-samples-data/generative-ai/audio/How_to_create_a_My_Map_in_Google_Maps.mp3",
                "audio/mpeg"
            )
            text("Please translate the audio to Mandarin.")
        }
    ),
    Sample(
        title = "Image generation",
        description = "Generate images with the Imagen 3 model",
        navRoute = "imagen",
        categories = listOf(Category.IMAGE),
        initialPrompt = content {
            text(
                "Generate a comicbook style image of" +
                        " Tower bridge with cherry blossoms around"
            )
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
    )
)
