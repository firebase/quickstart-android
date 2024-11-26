plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
    id("com.google.firebase.example.dataconnect.gradle")
}

android {
    namespace = "com.google.firebase.example.dataconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.firebase.example.dataconnect"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.coil.compose)

    // Firebase dependencies
    implementation(libs.firebase.auth)
    implementation(libs.firebase.dataconnect)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// This "dataconnect" section is a Gradle "extension" that is added by the
// "com.google.firebase.example.dataconnect.gradle" Gradle plugin. It must be
// present to specify configuration details for the Data Connect Gradle plugin.
dataconnect {
    // The version of Node.js (https://nodejs.org) to use to install and run the
    // Firebase CLI. This version of Node.js will be downloaded and extracted
    // for exclusive use of the Data Connect Gradle plugin.
    nodeVersion = "20.18.1"

    // The version of the Firebase CLI (https://www.npmjs.com/package/firebase-tools)
    // to use to perform the Data Connect Kotlin code generation.
    firebaseCliVersion = "13.25.0"

    // The directory that contains dataconnect.yaml that specifies the Data
    // Connect schema and connectors whose code to generate.
    dataConnectConfigDir = file("../dataconnect")
}
