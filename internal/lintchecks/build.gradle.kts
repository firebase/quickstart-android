plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    lint {
        targetSdk = 37
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
