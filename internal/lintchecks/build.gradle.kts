plugins {
    id("com.android.library")
}
android {
    namespace = "com.firebase.lintchecks"
    compileSdk = 37

    defaultConfig {
        minSdk = 16
    }

    lint {
        targetSdk = 37
    }
}

dependencies {
    lintChecks(project(":internal:lint"))
}
