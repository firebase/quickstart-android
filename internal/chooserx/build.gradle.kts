plugins {
    id("com.android.library")
}

android {
    namespace = "com.firebase.example.internal"
    compileSdk = 37

    defaultConfig {
        minSdk = 16

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        targetSdk = 37
    }

}

dependencies {
    api("com.google.android.material:material:1.14.0")
    api("androidx.recyclerview:recyclerview:1.4.0")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
}
