# Firebase AI Logic quickstart sample app

This Android sample app demonstrates how to use state-of-the-art
generative AI models (like Gemini) to build AI-powered features and applications.

For more information about Firebase AI Logic, visit the [documentation](http://firebase.google.com/docs/ai-logic).

## Setup & Configuration

### Prerequisites
*   **Google AI (Gemini) API Key**: Most samples work out of the box with the Google AI SDK.
*   **Vertex AI**: Samples marked with *(Vertex AI)* require you to enable the Vertex AI API in your Google Cloud project and have your files in Cloud Storage.
*   **Server Prompt Templates**: These samples require you to set up templates in the [Firebase Console](https://console.firebase.google.com/project/_/ai-logic).

## Getting Started

To try out this sample app, you need to use latest stable version of Android Studio.

* [Set up your Android app for Firebase][setup-android]
  * Use the package name `com.google.firebase.quickstart.ai`
* [Set up Firebase AI Logic][setup-ai-logic] 
* Run the app on an Android device or emulator.

## Features

You can find the implementation for each feature by clicking on the links below:

### Text / Chat
- [Travel tips](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/TravelTipsViewModel.kt): The user wants the model to help a new traveler with travel tips
- [Chatbot recommendations for courses](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/CourseRecommendationsViewModel.kt): A chatbot suggests courses for a performing arts program.
- [Weather Chat](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/WeatherChatViewModel.kt): Use function calling to get the weather conditions for a specific US city on a specific date.
- [Grounding with Google Search](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/GoogleSearchGroundingViewModel.kt): Use Grounding with Google Search to get responses based on up-to-date information from the web.
- [Thinking](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/ThinkingChatViewModel.kt): Gemini 2.5 Flash with dynamic thinking
- [Server Prompt Templates - Gemini](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/ServerPromptTemplateViewModel.kt): Generate an invoice using server prompt templates.

### Image analysis / generation
- [Imagen 4 - image generation](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenGenerationViewModel.kt): Generate images using Imagen 4
- [Imagen 3 - Inpainting (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenInpaintingViewModel.kt): Replace part of an image using Imagen 3
- [Imagen 3 - Outpainting (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenOutpaintingViewModel.kt): Expand an image by drawing in more background
- [Imagen 3 - Subject Reference (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenSubjectReferenceViewModel.kt): Generate an image using a referenced subject (must be an animal)
- [Imagen 3 - Style Transfer (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenStyleTransferViewModel.kt): Change the art style of a cat picture using a reference
- [Gemini 2.5 Flash Image (aka nanobanana)](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/ImageGenerationViewModel.kt): Generate and/or edit images using Gemini 2.5 Flash Image aka nanobanana
- [Server Prompt Template - Imagen](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenTemplateViewModel.kt): Generate an image using a server prompt template.
- [SVG Generator](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/SvgViewModel.kt): Use Gemini 3 Flash preview to create SVG illustrations
- [Blog post creator (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/ImageBlogCreatorViewModel.kt): Create a blog post from an image file stored in Cloud Storage.

### Audio analysis
- [Audio Summarization](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/AudioSummarizationViewModel.kt): Summarize an audio file
- [Translation from audio (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/AudioTranslationViewModel.kt): Translate an audio file stored in Cloud Storage

### Video analysis
- [Hashtags for a video (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/VideoHashtagGeneratorViewModel.kt): Generate hashtags for a video ad stored in Cloud Storage
- [Summarize video](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/VideoSummarizationViewModel.kt): Summarize a video and extract important dialogue.

### Live API (Real-time bidrectional streaming)
- [ForecastTalk](app/src/main/java/com/google/firebase/quickstart/ai/feature/live/BidiViewModel.kt): Use bidirectional streaming to get information about weather conditions
- [Gemini Live (Video input)](app/src/main/java/com/google/firebase/quickstart/ai/feature/live/BidiViewModel.kt): Use bidirectional streaming to chat with Gemini using your phone's camera

### Document (PDFs) analysis
- [Document comparison (Vertex AI)](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/DocumentComparisonViewModel.kt): Compare the contents of 2 documents in Cloud Storage.


## All samples

The full list of available samples can be found in the
[FirebaseAISamples.kt file](app/src/main/java/com/google/firebase/quickstart/ai/ui/navigation/FirebaseAISamples.kt).

[setup-android]: https://firebase.google.com/docs/android/setup
[setup-ai-logic]: https://firebase.google.com/docs/ai-logic/get-started?api=dev#set-up-firebase