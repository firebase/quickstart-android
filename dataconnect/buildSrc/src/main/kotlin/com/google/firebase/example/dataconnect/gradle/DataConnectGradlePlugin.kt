/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.example.dataconnect.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import java.util.Locale
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("unused")
abstract class DataConnectGradlePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val buildDirectory = project.layout.buildDirectory.dir("dataconnect")

    val setupFirebaseToolsTask =
      project.tasks.register<SetupFirebaseToolsTask>("setupFirebaseToolsForDataConnect") {
        version.set("13.23.0")
        outputDirectory.set(buildDirectory.map { it.dir("firebase-tools") })
      }
    val firebaseExecutable = setupFirebaseToolsTask.flatMap { it.firebaseExecutable }

    val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
    androidComponents.onVariants { variant ->
      val variantBuildDirectory = buildDirectory.map { it.dir("variants/${variant.name}") }
      this@DataConnectGradlePlugin.registerVariantTasks(
        project = project,
        variant = variant,
        buildDirectoryProvider = variantBuildDirectory,
        firebaseExecutableProvider = firebaseExecutable,
      )
    }
  }

  private fun registerVariantTasks(
    project: Project,
    variant: ApplicationVariant,
    buildDirectoryProvider: Provider<Directory>,
    firebaseExecutableProvider: Provider<RegularFile>,
  ) {
    val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }

    val generateCodeTask =
      project.tasks.register<GenerateCodeTask>(
        "generate${variantNameTitleCase}DataConnectSources"
      ) {
        inputDirectory.set(project.layout.projectDirectory.dir("../dataconnect"))
        firebaseExecutable.set(firebaseExecutableProvider)
        tweakedConnectorsDirectory.set(buildDirectoryProvider.map { it.dir("tweakedConnectors") })
      }

    variant.sources.java!!.addGeneratedSourceDirectory(
      generateCodeTask,
      GenerateCodeTask::outputDirectory,
    )
  }
}
