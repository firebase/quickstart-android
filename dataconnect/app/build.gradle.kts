import com.android.build.api.variant.AndroidComponentsExtension
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.google.firebase.example.dataconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.firebase.example.dataconnect"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets.getByName("main") {
        java.srcDirs("build/generated/sources")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.coil.compose)

    // Firebase dependencies
    implementation(libs.firebase.auth)
    implementation(libs.firebase.dataconnect)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

abstract class GenerateDataConnectSourcesTask : DefaultTask() {
  @get:InputFiles abstract val inputDirectory: DirectoryProperty
  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty
  @get:Internal abstract val workDirectory: DirectoryProperty

  @TaskAction
  fun run() {
    val inputDirectory = inputDirectory.get().asFile
    val outputDirectory = outputDirectory.get().asFile
    val workDirectory = workDirectory.get().asFile

    project.delete(outputDirectory)
    project.delete(workDirectory)

    project.copy {
      from(inputDirectory)
      into(workDirectory)
    }

    val connectorYamlFile = workDirectory.resolve("movie-connector/connector.yaml")
    val outputFileLineRegex = Regex("""(\s*outputDir:\s*).*""")
    val connectorYamlOriginalLines = connectorYamlFile.readLines(Charsets.UTF_8)
    val connectorYamlUpdatedLines = connectorYamlOriginalLines.map {
      val matchResult = outputFileLineRegex.matchEntire(it)
      if (matchResult === null) {
        it
      } else {
        matchResult.groupValues[1] + outputDirectory.absolutePath
      }
    }
    connectorYamlFile.writeText(connectorYamlUpdatedLines.joinToString("") { it + "\n" }, Charsets.UTF_8)

    val logFile = if (logger.isInfoEnabled) null else workDirectory.resolve("generate.log.txt")
    val logFileStream = logFile?.outputStream()
    try {
      project.exec {
        isIgnoreExitValue = false
        if (logFileStream !== null) {
          standardOutput = logFileStream
          errorOutput = logFileStream
        }
        workingDir(workDirectory)
        executable("firebase")
        args("--debug")
        args("dataconnect:sdk:generate")
        // Specify a fake project because dataconnect:sdk:generate unnecessarily
        // requires one. The actual value does not matter.
        args("--project", "zzyzx")
      }
    } catch (e: Exception) {
      logFileStream?.close()
      logFile?.forEachLine { logger.error(it.trimEnd()) }
    } finally {
      logFileStream?.close()
    }
  }
}

val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
androidComponents.onVariants { variant ->
  val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }
  val generateCodeTaskName = "generate${variantNameTitleCase}DataConnectSources"
  val generateCodeTask = tasks.register<GenerateDataConnectSourcesTask>(generateCodeTaskName) {
    inputDirectory.set(layout.projectDirectory.dir("../dataconnect"))
    outputDirectory.set(layout.buildDirectory.dir("generated/dataconnect/${variant.name}"))
    workDirectory.set(layout.buildDirectory.dir("intermediates/dataconnect/${variant.name}"))
  }
  variant.sources.java!!.addGeneratedSourceDirectory(
    generateCodeTask,
    GenerateDataConnectSourcesTask::outputDirectory,
  )
}
