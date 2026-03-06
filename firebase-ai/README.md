# Firebase AI Logic quickstart sample app

This Android sample app demonstrates how to use state-of-the-art
generative AI models (like Gemini) to build AI-powered features and applications.
For more information about Firebase AI Logic, visit the [documentation](http://firebase.google.com/docs/ai-logic).

## Getting Started

To try out this sample app, you need to use latest stable version of Android Studio.
However, if you want to latest lint checks and AI productivity features in Android
Studio use the latest preview version of [Android Studio](https://developer.android.com/studio/preview).

## Features

There are 2 main files that demonstrate the use of Firebase AI Logic:

- [ChatViewModel.kt](app/src/main/java/com/google/firebase/quickstart/ai/feature/text/ChatViewModel.kt)
 which can do things such as:
    -  [Generate Text](https://firebase.google.com/docs/ai-logic/generate-text)
    -  [Generate structured output (JSON)](https://firebase.google.com/docs/ai-logic/generate-structured-output)
    -  [Analyze images](https://firebase.google.com/docs/ai-logic/analyze-images)
    -  [Analyze video](https://firebase.google.com/docs/ai-logic/analyze-video)
    -  [Analyze audio](https://firebase.google.com/docs/ai-logic/analyze-audio)
    -  [Analyze documents (PDFs)](https://firebase.google.com/docs/ai-logic/analyze-documents)
    -  [Generate images using Gemini 2.0](https://firebase.google.com/docs/ai-logic/generate-images-imagen)
    -  [Function calling](https://firebase.google.com/docs/ai-logic/function-calling)
- [ImagenViewModel](app/src/main/java/com/google/firebase/quickstart/ai/feature/media/imagen/ImagenViewModel.kt)
 which shows how to [Generate images using Imagen models](https://firebase.google.com/docs/ai-logic/generate-images-imagen)

## All samples

The full list of available samples can be found in the
[FirebaseAISamples.kt file](app/src/main/java/com/google/firebase/quickstart/ai/FirebaseAISamples.kt). 
