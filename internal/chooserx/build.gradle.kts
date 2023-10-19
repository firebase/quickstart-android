plugins {
    id("com.android.library")
}

android {
    namespace = "com.firebase.example.internal"
    compileSdk = 34

    defaultConfig {
        minSdk = 16
        targetSdk = 34

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
    api("com.google.android.material:material:1.9.0")
    api("androidx.recyclerview:recyclerview:1.3.2")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
}
