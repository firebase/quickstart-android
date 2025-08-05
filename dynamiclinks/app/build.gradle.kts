import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

tasks {
    check.dependsOn("assembleMainFlavorDebugAndroidTest")
}

android {
    namespace = "com.google.firebase.quickstart.deeplinks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.deeplinks"
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

    flavorDimensions +=  "all"

    productFlavors {
        create("mainFlavor") {
            dimension = "all"

            // TODO(developer): Replace this with your Dynamic Links URI prefix
            //                  See: https://firebase.google.com/docs/dynamic-links/android/create#set-up-firebase-and-the-dynamic-links-sdk
            resValue("string", "dynamic_links_uri_prefix", "https://YOUR_APP.page.link")
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

    implementation("com.google.android.material:material:1.12.0")

    // Firebase Dynamic Links
    implementation("com.google.firebase:firebase-dynamic-links:22.1.0")

    // For an optimal experience using Dynamic Links, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics:23.0.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
