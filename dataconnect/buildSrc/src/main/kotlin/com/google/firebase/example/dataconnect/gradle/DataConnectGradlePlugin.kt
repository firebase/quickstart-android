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
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask
import com.google.firebase.example.dataconnect.gradle.tasks.ExtractArchiveTask
import com.google.firebase.example.dataconnect.gradle.tasks.GenerateDataConnectSourcesTask
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
import java.util.Locale
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("unused")
public abstract class DataConnectGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val downloadNodeJsArchiveTask = project.tasks.register<DownloadNodeJsBinaryDistributionArchiveTask>(
            "dataConnectDownloadNodeJs"
        )
        val extractNodeJsArchiveTask = project.tasks.register<ExtractArchiveTask>("dataConnectExtractNodeJs")
        val setupFirebaseToolsTask = project.tasks.register<SetupFirebaseToolsTask>("dataConnectSetupFirebaseTools")

        val dataConnectExtension = project.extensions.create("dataconnect", DataConnectExtension::class.java)
        val configurer = DataConnectTasksConfigurer(
            dataConnectExtension = dataConnectExtension,
            downloadNodeJsArchiveTask = downloadNodeJsArchiveTask,
            extractNodeJsArchiveTask = extractNodeJsArchiveTask,
            setupFirebaseToolsTask = setupFirebaseToolsTask,
            buildDirectory = project.layout.buildDirectory.dir("dataConnect"),
            projectDirectory = project.layout.projectDirectory,
            providerFactory = project.providers
        )

        configurer.invoke()

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants { variant ->
            val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }
            val generateCodeTaskName = "dataConnectGenerate${variantNameTitleCase}Sources"
            val generateCodeTask = project.tasks.register<GenerateDataConnectSourcesTask>(generateCodeTaskName)
            configurer.configureGenerateDataConnectSourcesTask(generateCodeTask, variantNameTitleCase)

            variant.sources.java!!.addGeneratedSourceDirectory(
                generateCodeTask,
                GenerateDataConnectSourcesTask::outputDirectory
            )
        }
    }
}
