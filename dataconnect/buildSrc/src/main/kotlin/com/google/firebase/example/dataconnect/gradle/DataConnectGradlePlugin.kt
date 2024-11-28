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
import com.google.firebase.example.dataconnect.gradle.providers.MyProjectProviders
import com.google.firebase.example.dataconnect.gradle.providers.MyVariantProviders
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsBinaryDistributionArchiveTask
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsTask
import com.google.firebase.example.dataconnect.gradle.tasks.GenerateDataConnectSourcesTask
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
import com.google.firebase.example.dataconnect.gradle.tasks.configureFrom
import java.util.Locale
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

@Suppress("unused")
abstract class DataConnectGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val downloadNodeJsTask = project.tasks.register<DownloadNodeJsTask>("downloadNodeJs")
        val setupFirebaseToolsTask = project.tasks.register<SetupFirebaseToolsTask>("setupFirebaseToolsForDataConnect")

        project.extensions.create("dataconnect", DataConnectExtension::class.java)
        val providers = MyProjectProviders(project, downloadNodeJsTask)

        project.tasks.register<DownloadNodeJsBinaryDistributionArchiveTask>("downloadNodeJsBinaryDistributionArchive") {
            group = TASK_GROUP
            configureFrom(providers)
        }
        downloadNodeJsTask.configure {
            group = TASK_GROUP
            configureFrom(providers)
        }
        setupFirebaseToolsTask.configure {
            group = TASK_GROUP
            configureFrom(providers)
        }

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants { variant ->
            val variantProviders = MyVariantProviders(variant, setupFirebaseToolsTask, providers)
            registerVariantTasks(project, variant, variantProviders)
        }
    }

    private fun registerVariantTasks(
        project: Project,
        variant: ApplicationVariant,
        providers: MyVariantProviders
    ) {
        val variantNameTitleCase = variant.name.replaceFirstChar { it.titlecase(Locale.US) }

        val generateCodeTaskName = "generate${variantNameTitleCase}DataConnectSources"
        val generateCodeTask = project.tasks.register<GenerateDataConnectSourcesTask>(generateCodeTaskName) {
            configureFrom(providers)
            @Suppress("ktlint:standard:max-line-length")
            setOnlyIf(
                "dataconnect.dataConnectConfigDir is null; to enable the \"$name\" task, " +
                    "set dataconnect.dataConnectConfigDir in build.gradle or build.gradle.kts to " +
                    "the directory that defines the Data Connect schema and " +
                    "connectors whose Kotlin code to generate code. That is, the directory " +
                    "containing the dataconnect.yaml file. For details, see " +
                    "https://firebase.google.com/docs/data-connect/configuration-reference#dataconnect.yaml-configuration " +
                    "(e.g. file(\"../dataconnect\")) (message code a3ch245mbd)"
            ) { dataConnectConfigDir.isPresent }
        }

        variant.sources.java!!.addGeneratedSourceDirectory(
            generateCodeTask,
            GenerateDataConnectSourcesTask::outputDirectory
        )
    }

    companion object {
        private const val TASK_GROUP = "Firebase Data Connect"
    }
}
