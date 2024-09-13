plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk = 35

    defaultConfig {
        minSdk = 16
        targetSdk = 35
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
