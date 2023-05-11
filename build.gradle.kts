import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.4" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("androidx.navigation.safeargs") version "2.5.3" apply false
    id("com.github.ben-manes.versions") version "0.41.0" apply false
}

allprojects {
    repositories {
        google()
        //mavenLocal() must be listed at the top to facilitate testing
        mavenLocal()
        mavenCentral()
    }
}

//configurations {
//    ktlint
//}

//dependencies {
//    ktlint ("com.pinterest:ktlint:0.49.0") {
//        attributes {
//            attribute(Bundling.BUNDLING_ATTRIBUTE, getObjects().named(Bundling, Bundling.EXTERNAL))
//        }
//    }
//}

//task("ktlint", type: JavaExec, group: "verification") {
//    def outputDir = "${project.buildDir}/reports/ktlint/"
//    def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
//    def outputFile = "${outputDir}ktlint-checkstyle-report.xml"
//
//    // See:
//    // https://medium.com/@vanniktech/making-your-gradle-tasks-incremental-7f26e4ef09c3
//    inputs.files(inputFiles)
//    outputs.dir(outputDir)
//
//    description = "Check Kotlin code style."
//    classpath = configurations.ktlint
//    mainClass.set("com.pinterest.ktlint.Main")
//    args = [
//        "--format",
//        "--code-style=android_studio",
//        "--reporter=plain",
//        "--reporter=checkstyle,output=${outputFile}",
//        "**/*.kt",
//    ]
//    jvmArgs "--add-opens=java.base/java.lang=ALL-UNNAMED"
//}

fun isNonStable(candidate: ModuleComponentIdentifier): Boolean {
    return listOf("alpha", "beta", "rc", "snapshot", "-m").any { keyword ->
        candidate.version.toLowerCase().contains(keyword)
    }
}

fun isBlackListed(candidate: ModuleComponentIdentifier): Boolean {
    return listOf(
            "androidx.browser:browser",
            "com.facebook.android",
            "com.google.guava",
            "com.github.bumptech.glide"
    ).any { keyword ->
        candidate.toString().contains(keyword)
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate) || isBlackListed(candidate)
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
