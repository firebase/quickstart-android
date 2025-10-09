import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.google.firebase.appdistributionquickstart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.firebase.appdistributionquickstart"
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
    lint {
        warning.add("InvalidPackage")
        // TODO(thatfiredev): Remove this once
        //  https://github.com/bumptech/glide/issues/4940 is fixed
        //  (and the same change gets applied to App Distribution)
        disable.add("NotificationPermission")
    }
}

dependencies {
    implementation(project(":internal:lintchecks"))
    implementation(project(":internal:chooserx"))

    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.multidex:multidex:2.0.1")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // ADD the SDK to the "prerelease" variant only (example)
    implementation("com.google.firebase:firebase-appdistribution:16.0.0-beta17")

    // For an optimal experience using App Distribution, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics")

    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
