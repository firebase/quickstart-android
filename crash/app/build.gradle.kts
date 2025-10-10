import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

tasks {
    check.dependsOn("assembleDebugAndroidTest")
}

android {
    namespace = "com.google.samples.quickstart.crash"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.samples.quickstart.crash"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "test-proguard-rules.pro")
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
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-ktx:1.11.0")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")

    // For an optimal experience using Crashlytics, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics")

    // For use in the CustomKeySamples -- for testing Google Api Availability.
    implementation("com.google.android.gms:play-services-base:18.5.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
