import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

tasks {
    check.dependsOn("assembleDebugAndroidTest")
}

android {
    namespace= "com.google.firebase.quickstart.auth"
    compileSdk = 34
    flavorDimensions += "minSdkVersion"

    defaultConfig {
        applicationId = "com.google.firebase.quickstart.auth"
        minSdk = 19
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":internal:chooserx"))
    implementation(project(":internal:lintchecks"))
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Google Identity Services SDK (only required for Auth with Google)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Firebase UI
    // Used in FirebaseUIActivity.
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Facebook Android SDK (only required for Facebook Login)
    // Used in FacebookLoginActivity.
    implementation("com.facebook.android:facebook-login:13.2.0")
    implementation("androidx.browser:browser:1.5.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
}
