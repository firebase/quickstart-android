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

package com.google.firebase.example.dataconnect.gradle.providers

import com.android.build.api.variant.ApplicationVariant
import com.google.firebase.example.dataconnect.gradle.DataConnectExtension
import com.google.firebase.example.dataconnect.gradle.tasks.DownloadNodeJsTask
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

internal open class MyProjectProviders(
    projectBuildDirectory: DirectoryProperty,
    val providerFactory: ProviderFactory,
    val objectFactory: ObjectFactory,
    projectDirectoryHierarchy: List<Directory>,
    val nodeExecutable: Provider<RegularFile>,
    val npmExecutable: Provider<RegularFile>,
    ext: DataConnectExtension,
    logger: Logger
) {

    constructor(
        project: Project,
        downloadNodeJsTask: TaskProvider<DownloadNodeJsTask>
    ) : this(
        projectBuildDirectory = project.layout.buildDirectory,
        providerFactory = project.providers,
        objectFactory = project.objects,
        projectDirectoryHierarchy = project.projectDirectoryHierarchy(),
        nodeExecutable = downloadNodeJsTask.map { it.nodeOutputFiles.nodeExecutable },
        npmExecutable = downloadNodeJsTask.map { it.nodeOutputFiles.npmExecutable },
        ext = project.extensions.getByType<DataConnectExtension>(),
        project.logger
    )

    val operatingSystem: Provider<OperatingSystem> = OperatingSystem.provider(objectFactory, providerFactory, logger)

    val buildDirectory: Provider<Directory> = projectBuildDirectory.map { it.dir("dataconnect") }

    val firebaseToolsVersion: Provider<String> =
        providerFactory.provider {
            ext.firebaseToolsVersion
                ?: throw GradleException(
                    "dataconnect.firebaseToolsVersion must be set in your " +
                        "build.gradle or build.gradle.kts " +
                        "(error code xbmvkc3mtr)"
                )
        }

    private val localConfigProviders = LocalConfigProviders(projectDirectoryHierarchy, providerFactory, logger)
}

internal open class MyVariantProviders(
    variant: ApplicationVariant,
    val projectProviders: MyProjectProviders,
    val firebaseExecutable: Provider<RegularFile>,
    ext: DataConnectExtension,
    objectFactory: ObjectFactory
) {

    constructor(
        project: Project,
        variant: ApplicationVariant,
        setupFirebaseToolsTask: TaskProvider<SetupFirebaseToolsTask>,
        projectProviders: MyProjectProviders
    ) : this(
        variant = variant,
        projectProviders = projectProviders,
        firebaseExecutable = setupFirebaseToolsTask.flatMap { it.firebaseExecutable },
        ext = project.extensions.getByType<DataConnectExtension>(),
        objectFactory = project.objects
    )

    val buildDirectory: Provider<Directory> =
        projectProviders.buildDirectory.map { it.dir("variants/${variant.name}") }

    val dataConnectConfigDir: Provider<Directory> = run {
        val dir =
            ext.dataConnectConfigDir
                ?: throw GradleException(
                    "dataconnect.dataConnectConfigDir must be set in your build.gradle or build.gradle.kts " +
                        "(error code xbmvkc3mtr)"
                )
        objectFactory.directoryProperty().also { property -> property.set(dir) }
    }
}

private fun Project.projectDirectoryHierarchy(): List<Directory> = buildList {
    var curProject: Project? = this@projectDirectoryHierarchy
    while (curProject !== null) {
        add(curProject.layout.projectDirectory)
        curProject = curProject.parent
    }
}
