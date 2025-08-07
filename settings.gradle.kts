pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":admob:app",
        ":firebase-ai:app",
        ":analytics:app",
        ":appdistribution:app",
        ":auth:app",
        ":config:app",
        ":crash:app",
        ":database:app",
        ":dataconnect:app",
        ":firestore:app",
        ":functions:app",
        ":internal:chooserx",
        ":internal:lint",
        ":internal:lintchecks",
        ":inappmessaging:app",
        ":messaging:app",
        ":perf:app",
        ":storage:app"
)
