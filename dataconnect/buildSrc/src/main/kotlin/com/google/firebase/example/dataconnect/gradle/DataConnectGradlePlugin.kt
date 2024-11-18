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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.util.Locale

@Suppress("unused")
abstract class DataConnectGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants {
            this@DataConnectGradlePlugin.registerTasks(project, it)
        }
    }

    private fun registerTasks(project: Project, variant: ApplicationVariant) {
        val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }
        val buildDirectory = project.layout.buildDirectory.dir("dataconnect/${variant.name}")

        val setupFirebaseToolsTask = project.tasks.register<SetupFirebaseToolsTask>("setupFirebaseTools$variantNameTitleCase") {
            version.set("13.23.0")
            outputDirectory.set(buildDirectory.map { it.dir("firebase-tools") })
        }

        val generateCodeTask = project.tasks.register<GenerateCodeTask>("generateDataConnectSources${variantNameTitleCase}") {
            inputDirectory.set(project.layout.projectDirectory.dir("../dataconnect"))
            firebaseExecutable.set(setupFirebaseToolsTask.flatMap { it.firebaseExecutable })
            workDirectory.set(buildDirectory.map { it.dir("generateCodeTaskWorkDir") })
        }

        variant.sources.java!!.addGeneratedSourceDirectory(
            generateCodeTask,
            GenerateCodeTask::outputDirectory,
        )
    }
}
