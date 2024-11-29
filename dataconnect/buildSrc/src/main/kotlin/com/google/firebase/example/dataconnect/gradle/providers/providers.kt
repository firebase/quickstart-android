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
import com.google.firebase.example.dataconnect.gradle.tasks.NodeJsPaths
import com.google.firebase.example.dataconnect.gradle.tasks.SetupFirebaseToolsTask
import com.google.firebase.example.dataconnect.gradle.tasks.nodeJsPaths
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
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
    val projectLayout: ProjectLayout,
    ext: DataConnectExtension,
    logger: Logger
) {

    constructor(
        project: Project,
    ) : this(
        projectBuildDirectory = project.layout.buildDirectory,
        providerFactory = project.providers,
        objectFactory = project.objects,
        projectLayout = project.layout,
        ext = project.extensions.getByType<DataConnectExtension>(),
        project.logger
    )

    val operatingSystem: Provider<OperatingSystem> = providerFactory.operatingSystem(logger)

    val buildDirectory: Provider<Directory> = projectBuildDirectory.map { it.dir("dataconnect") }

    val firebaseCliVersion: Provider<String> = run {
        val lazyFirebaseCliVersion: Lazy<String> = lazy {
            ext.firebaseCliVersion
                ?: throw GradleException(
                    "dataconnect.firebaseCliVersion must be set in " +
                        "build.gradle or build.gradle.kts to " +
                        "specify the version of the Firebase CLI npm package " +
                        "(https://www.npmjs.com/package/firebase-tools) to use " +
                        "(e.g. \"13.25.0\") (error code xbmvkc3mtr)"
                )
        }
        providerFactory.provider { lazyFirebaseCliVersion.value }
    }

    val nodeJsVersion: Provider<String> = run {
        val lazyNodeVersion: Lazy<String> = lazy {
            ext.nodeJsVersion
                ?: throw GradleException(
                    "dataconnect.nodeJsVersion must be set in " +
                        "build.gradle or build.gradle.kts to " +
                        "specify the version of Node.js (https://nodejs.org) " +
                        "to install (e.g. \"20.9.0\") (error code 3acj27az2c)"
                )
        }
        providerFactory.provider { lazyNodeVersion.value }
    }

    val dataConnectConfigDir: Provider<Directory> = run {
        val lazyDataConnectConfigDir: Lazy<Directory?> = lazy {
            ext.dataConnectConfigDir?.let {
                projectLayout.projectDirectory.dir(it.path)
            }
        }
        providerFactory.provider { lazyDataConnectConfigDir.value }
    }

    val nodeJsPaths: Provider<NodeJsPaths> = providerFactory.nodeJsPaths(nodeJsVersion, operatingSystem)
}

internal open class MyVariantProviders(
    variant: ApplicationVariant,
    val projectProviders: MyProjectProviders,
    val firebaseExecutable: Provider<RegularFile>
) {

    constructor(
        variant: ApplicationVariant,
        setupFirebaseToolsTask: TaskProvider<SetupFirebaseToolsTask>,
        projectProviders: MyProjectProviders
    ) : this(
        variant = variant,
        projectProviders = projectProviders,
        firebaseExecutable = setupFirebaseToolsTask.map { it.firebaseExecutable }
    )

    val buildDirectory: Provider<Directory> =
        projectProviders.buildDirectory.map { it.dir("variants/${variant.name}") }
}
