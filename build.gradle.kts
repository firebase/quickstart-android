import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.gradle.versions) apply true
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless) apply true
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
}

fun notFromFirebase(candidate: ModuleComponentIdentifier): Boolean {
    return candidate.group != "com.google.firebase"
}

fun isNonStable(candidate: ModuleComponentIdentifier): Boolean {
    return listOf("alpha", "beta", "rc", "snapshot", "-m", "final").any { keyword ->
        keyword in candidate.version.lowercase()
    }
}

fun isBlockListed(candidate: ModuleComponentIdentifier): Boolean {
    return listOf(
            "androidx.browser:browser",
            "androidx.webkit:webkit",
            "com.facebook.android",
            "com.google.guava",
            "com.github.bumptech.glide"
    ).any { keyword ->
        keyword in candidate.toString().lowercase()
    }
}

// TODO(b/522845800): remove this once the bug with AGP 9.3.x and lint has been fixed
fun isBuggyAGP(candidate: ModuleComponentIdentifier): Boolean {
  // Skip 'com.android.application' and 'com.android.library' versions <= 9.3.1
  return candidate.toString().lowercase().contains("com.android.") &&
          candidate.version.replace(".", "").toInt() <= 931
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        (isNonStable(candidate) && notFromFirebase(candidate)) || isBlockListed(candidate) ||
                isBuggyAGP(candidate)
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.layout.buildDirectory)
    }
}
