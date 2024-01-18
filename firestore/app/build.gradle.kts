plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.google.firebase.example.fireeats"
    testBuildType = "release"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.google.firebase.example.fireeats"
        minSdk = 19
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            testProguardFiles(getDefaultProguardFile("proguard-android.txt"), "test-proguard-rules.pro")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
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

    lint {
        disable += "InvalidPackage"
        // TODO(thatfiredev): Remove this once
        //  https://github.com/bumptech/glide/issues/4940 is fixed
        disable += "NotificationPermission"
    }
}

dependencies {
    implementation(project(":internal:lintchecks"))
    implementation(project(":internal:chooserx"))

    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))

    // Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Google Play services
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // FirebaseUI (for authentication)
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Support Libs
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.browser:browser:1.5.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Android architecture components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.7.0")

    // Third-party libraries
    implementation("me.zhanghai.android.materialratingbar:library:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("com.google.firebase:firebase-auth")
}
