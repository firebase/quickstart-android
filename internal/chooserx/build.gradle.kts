plugins {
    id("com.android.library")
}

android {
    namespace = "com.firebase.example.internal"
    compileSdk = 36

    defaultConfig {
        minSdk = 16
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    api("com.google.android.material:material:1.12.0")
    api("androidx.recyclerview:recyclerview:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
}
