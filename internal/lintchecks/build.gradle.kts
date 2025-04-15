plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk = 36

    defaultConfig {
        minSdk = 16
        targetSdk = 36
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
