import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

tasks {
    check.dependsOn("assembleDebugAndroidTest")
}

android {
    namespace = "com.google.firebase.quickstart.firebasestorage"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.firebasestorage"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":internal:lintchecks"))
    implementation(project(":internal:chooserx"))

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Cloud Storage for Firebase
    implementation("com.google.firebase:firebase-storage")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}
