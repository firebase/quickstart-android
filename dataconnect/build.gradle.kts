// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.layout.buildDirectory)
    }

    register<Exec>("dataconnectCompile") {
        workingDir = project.file("./dataconnect")
        if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)) {
            commandLine("npx.cmd", "-y", "firebase-tools@latest", "dataconnect:compile")
        } else {
            commandLine("npx", "-y", "firebase-tools@latest", "dataconnect:compile")
        }
        isIgnoreExitValue = true
    }

    named("clean") {
        finalizedBy("dataconnectCompile")
    }
}