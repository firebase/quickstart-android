plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk = 34

    defaultConfig {
        minSdk = 16
        targetSdk = 34
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
