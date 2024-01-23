// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

allprojects {
    repositories {
         //mavenLocal() must be listed at the top to facilitate testing
        mavenLocal()
        google()
        mavenCentral()
    }
}
