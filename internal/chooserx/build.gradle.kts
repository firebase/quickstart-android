plugins {
    id("com.android.library")
}

android {
    namespace = "com.firebase.example.internal"
    compileSdk = 35

    defaultConfig {
        minSdk = 16
        targetSdk = 35

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
    api("androidx.recyclerview:recyclerview:1.3.2")
    api("androidx.constraintlayout:constraintlayout:2.2.0")
}
