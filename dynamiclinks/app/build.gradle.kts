import com.android.build.gradle.internal.tasks.factory.dependsOn

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
    compileSdk = 33

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.deeplinks"
        minSdk = 19
        targetSdk = 33
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
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":internal:lintchecks"))
    implementation(project(":internal:chooserx"))

    implementation("com.google.android.material:material:1.9.0")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))

    // Firebase Dynamic Links (Java)
    implementation("com.google.firebase:firebase-dynamic-links")

    // Firebase Dynamic Links (Kotlin)
    implementation("com.google.firebase:firebase-dynamic-links-ktx")

    // For an optimal experience using Dynamic Links, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
