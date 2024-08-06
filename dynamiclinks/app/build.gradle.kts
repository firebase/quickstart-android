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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.deeplinks"
        minSdk = 21
        targetSdk = 34
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

    implementation("com.google.android.material:material:1.12.0")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Dynamic Links
    implementation("com.google.firebase:firebase-dynamic-links")

    // For an optimal experience using Dynamic Links, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation("com.google.firebase:firebase-analytics")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
