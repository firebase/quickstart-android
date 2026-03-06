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
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.49.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

tasks.register<JavaExec>("ktlintCheck") {
    val outputDir = "${project.layout.buildDirectory}/reports/ktlint/"
    val inputFiles = project.fileTree("src").include("**/*.kt")
    val outputFile = "${outputDir}ktlint-checkstyle-report.xml"

    // See: https://medium.com/@vanniktech/making-your-gradle-tasks-incremental-7f26e4ef09c3
    inputs.files(inputFiles)
    outputs.file(outputFile)

    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")

    args(
        "--format",
        "--code-style=android_studio",
        "--reporter=plain",
        "--reporter=checkstyle,output=${outputFile}",
        "**/*.kt",
        "!**/build/**"
    )

    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
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
            "com.github.bumptech.glide",
            "com.google.android.gms"
    ).any { keyword ->
        keyword in candidate.toString().lowercase()
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        (isNonStable(candidate) && notFromFirebase(candidate)) || isBlockListed(candidate)
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.layout.buildDirectory)
    }
}
