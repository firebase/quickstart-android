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
    namespace = "com.google.firebase.quickstart.fcm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.fcm"
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
    packaging {
        resources.excludes.add("LICENSE.txt")
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
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":internal:lintchecks"))
    implementation(project(":internal:chooserx"))
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.vectordrawable:vectordrawable-animated:1.2.0")
    implementation("androidx.core:core-ktx:1.17.0")

    // Required when asking for permission to post notifications (starting in Android 13)
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    implementation("com.google.android.material:material:1.13.0")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging")

    // For an optimal experience using FCM, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-installations:19.0.1")

    implementation("androidx.work:work-runtime:2.10.5")

    // Testing dependencies
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.annotation:annotation:1.9.1")
}
