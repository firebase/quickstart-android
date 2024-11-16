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

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.util.Locale

@Suppress("unused")
abstract class DataConnectGradlePlugin : Plugin<Project> {

    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        logger.info("Found AndroidComponentsExtension: ${androidComponents::class.qualifiedName}")
        logger.info("Android Gradle Plugin (AGP) Version: ${androidComponents.pluginVersion.version}")

        val generateDataConnectSourcesVariantTasks = mutableListOf<TaskProvider<GenerateDataConnectSourcesTask>>()
        androidComponents.onVariants { variant ->
            val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }
            val generateCodeTaskName = "generateDataConnectSources$variantNameTitleCase"

            logger.info("Registering Gradle task: $generateCodeTaskName")
            val generateCodeTask = project.tasks.register<GenerateDataConnectSourcesTask>(generateCodeTaskName) {
                inputDirectory.set(project.layout.projectDirectory.dir("../dataconnect"))
                workDirectory.set(project.layout.buildDirectory.dir("intermediates/dataconnect/${variant.name}"))
            }
            generateDataConnectSourcesVariantTasks.add(generateCodeTask)

            variant.sources.java!!.addGeneratedSourceDirectory(
                generateCodeTask,
                GenerateDataConnectSourcesTask::outputDirectory,
            )
        }

        androidComponents.selector()

        val generateDataConnectSourcesTaskName = "generateDataConnectSources"
        logger.info("Registering Gradle task: $generateDataConnectSourcesTaskName")
        project.tasks.register(generateDataConnectSourcesTaskName) { task ->
            generateDataConnectSourcesVariantTasks.forEach {
                task.dependsOn(it)
            }
        }
    }
}
