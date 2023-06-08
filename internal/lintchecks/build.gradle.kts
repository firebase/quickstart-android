plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk = 33

    defaultConfig {
        minSdk = 16
        targetSdk = 33
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
